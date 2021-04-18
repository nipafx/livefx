import {
	CalendarStructure,
	Cell,
	Entry,
	GridArea,
	GriddedEntry,
	GridStyle,
	Holiday,
	Month,
	Person,
	ThemedYear
} from "./types";
import { DateTime, Info, Interval } from "luxon";
import { arrayTo, splitByMonth } from "./functions";
import { NO_PERSON } from "./calendar";

const style = require('./calendar.module.css')

export const createCalendar = (year: number, entries: Entry[], holidays: Holiday[], people: Person[], themes: ThemedYear[]): CalendarStructure => {
	const peopleWithUnknown = [ ...people, { name: "", abbreviation: NO_PERSON } ]
		.map((person, index) => ({ ...person, indexInPeople: index }))

	const { blockedMonths, griddedEntries } = createEntries(entries, peopleWithUnknown)
	griddedEntries.unshift(...createCalendarStructure(holidays, year, blockedMonths))

	return {
		griddedEntries,
		gridStyle: computeGridStyle(blockedMonths, themes),
		months: createMonths(),
		people: peopleWithUnknown,
	}
}

const createMonths = (): Month[] => {
	const names = Info.months('long')
	const abbreviations = Info.months('short')
	return arrayTo(12).map(number => ({
		name: names[number],
		abbreviation: abbreviations[number],
		number: ++number
	}))
}

const createEntries = (entries: Entry[], people: Person[]): CalendarDataStructure => {
	const griddedEntries: GriddedEntry[] = []
	const months: MonthBlock[] = Info
		.months('short')
		.map(month => ({
			abbreviation: month,
			people: people.map((person, indexInPeople) => ({
				...person,
				indexInPeople,
				columns: [ { intervals: [] } ]
			}))
		}))

	const processEntry = (entry: Entry, entryIndex: number, entrySplit: Interval, person?: Person) => {
		const month = months[entrySplit.start.month - 1]
		const monthPerson: PersonWithColumns = month.people.find(p => p.abbreviation === (person?.abbreviation ?? NO_PERSON))!
		const columnIndex = computeColumnIndex(monthPerson.columns, entrySplit)
		const griddedEntry: GriddedEntry = {
			color: entry.category.color,
			className: style.entry,
			gridArea: computeGridAreaFromInterval(monthPerson.abbreviation, columnIndex, entrySplit),
			reactKey: computeReactKeyFromInterval(monthPerson.abbreviation, columnIndex, entrySplit),
			coordinates: { entryIndex }
		}
		if (columnIndex === monthPerson.columns.length) monthPerson.columns.push({ intervals: [] })

		monthPerson.columns[columnIndex].intervals.push(entrySplit)
		griddedEntries.push(griddedEntry)
	}

	entries.forEach((entry, entryIndex) => {
		if (entry.people.length === 0)
			splitByMonth(entry.start, entry.length)
				.forEach(entrySplit => processEntry(entry, entryIndex, entrySplit))
		entry.people
			.forEach(person => splitByMonth(entry.start, entry.length)
				.forEach(entrySplit => processEntry(entry, entryIndex, entrySplit, person)))
	})

	return { blockedMonths: months, griddedEntries }
}

const computeColumnIndex = (columns: IntervalColumn[], entrySplit: Interval): number => {
	for (let columnIndex = 0; columnIndex < columns.length; columnIndex++) {
		const fitsIntoColumn = !columns[columnIndex].intervals.find(split => split.overlaps(entrySplit))
		if (fitsIntoColumn) return columnIndex
	}

	return columns.length
}

const createCalendarStructure = (holidays: Holiday[], year: number, months: MonthBlock[]): GriddedEntry[] => {
	// don't pass `date` as a `LocalDate` because some combinations that exist as cells are invalid dates (e.g. 2021-02-30)
	const processCalendarStructure = (year: number, month: number, day: number, person: Person, personIndex: number, columnSpan: number, holidays: Holiday[]): GriddedEntry => {
		const cell: Cell = { month, day, personIndex }
		return {
			className: dayClassName(holidays, DateTime.local(year, month, day)),
			gridArea: computeGridArea(person.abbreviation, 0, month, day, columnSpan, 1),
			reactKey: computeReactKey(person.abbreviation, 0, `${month - 1}-${day}`),
			coordinates: { cell }
		}
	}

	return months
		.flatMap((month: MonthBlock, monthIndex: number) => month
			.people
			.flatMap((person: PersonWithColumns, personIndex: number) => arrayTo(31)
				.map(dayIndex => processCalendarStructure(year, monthIndex + 1, dayIndex + 1, person, personIndex, person.columns.length, holidays))))
}

const dayClassName = (holidays: Holiday[], date: DateTime): string => {
	// the order of the following checks is important:
	// only one div will be created per grid cell, so later types of divs (e.g. for weekends)
	// won't be created if an earlier check was true (e.g. holidays)

	if (!date.isValid) return style.nonDay

	const isHoliday = holidays
		.map(holiday => holiday.date)
		.some(holiday => holiday.equals(date))
	if (isHoliday) return style.holiday


	const isWeekendDay = date.weekday === 6 || date.weekday === 7
	if (isWeekendDay) return style.weekendDay

	return style.emptyDay
}

const computeGridStyle = (months: MonthBlock[], themes: ThemedYear[]): GridStyle => {
	const totalColumns = months
		.flatMap(month => month.people.map(person => person.columns.length))
		.reduce((result, columns) => result + columns, 0)

	const monthRowRaw = months
		.flatMap(month => month
			.people
			.flatMap(person => arrayTo(person.columns.length)
				.map(_ => `${month.abbreviation}`)
			))
		.join(" ")
	// add first column for the days of the month
	const monthRow = `' . ${monthRowRaw}'`

	const personRowRaw = months
		.flatMap(month => month
			.people
			.flatMap(person => arrayTo(person.columns.length)
				.map(_ => `${month.abbreviation}_${person.abbreviation}`)
			))
		.join(" ")
	// add first column for the days of the month
	const personRow = `' . ${personRowRaw}'`

	const dayRows = arrayTo(31)
		.map(day => {
			const dayRow = months
				.flatMap(month => month.people
					.flatMap(person => person.columns
						.map((_, columnIndex) =>
							`${month.abbreviation}_${person.abbreviation}_c${columnIndex}_d${day + 1}`)))
				.join(" ")
			return `'d_${day + 1} ${dayRow}'`
		})
		.join(" ")

	const themeRows = themes
		.map((_, themeIndex) => {
			const themeRow = months
				.flatMap(month => month
					.people
					.flatMap(person => arrayTo(person.columns.length)
						.map(_ => `theme_${themeIndex}_${month.abbreviation}`)
					))
				.join(" ")
			return `'. ${themeRow}'`
		})
		.join(" ")

	return {
		// first rows for month/people header, last rows for themes
		gridTemplateRows: `auto auto repeat(31, 1fr) repeat(${themes.length}, auto)`,
		// first column for the days of the month
		gridTemplateColumns: `auto repeat(${totalColumns}, 1fr)`,
		gridTemplateAreas: `${monthRow} ${personRow} ${dayRows} ${themeRows}`
	}
}

const computeGridAreaFromInterval = (person: string, columnIndex: number, interval: Interval): GridArea =>
	computeGridArea(person, columnIndex, interval.start.month, interval.start.day, 1, interval.length(`day`))

const computeGridArea = (person: string, columnIndex: number, month: number, day: number, columnSpan: number, rowSpan: number): GridArea => {
	const monthAbbreviation = Info.months('short')[month - 1]
	const column = `${monthAbbreviation}_${person}_c${columnIndex}`
	return {
		gridColumn: `${column}_d${day} / span ${columnSpan}`,
		gridRow: `${column}_d${day} / span ${rowSpan}`
	}
}

const computeReactKeyFromInterval = (person: string, columnIndex: number, interval: Interval): string =>
	computeReactKey(person, columnIndex, interval.start.toFormat(`MM-dd`))

const computeReactKey = (person: string, columnIndex: number, date: string): string =>
	`${person}_${columnIndex}_${date}`

/*
 * TYPES
 */

interface CalendarDataStructure {
	griddedEntries: GriddedEntry[]
	blockedMonths: MonthBlock[]
}

interface MonthBlock {
	abbreviation: string
	people: PersonWithColumns[]
}

interface PersonWithColumns extends Person {
	columns: IntervalColumn[]
}

interface IntervalColumn {
	intervals: Interval[]
}
