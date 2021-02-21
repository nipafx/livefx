import React, { useMemo, useState } from 'react'
import { DateTime, Duration, Info, Interval } from 'luxon'

import style from './calendar.module.css'

const NO_PERSON = "NONE"

const Calendar = ({ year, entries, holidays, people: teamMembers, setHoveredEntry, setSelectedEntry }) => {

	const [ highlightWithoutEntry, setHighlight ] = useState({
		entryIndex: -1,
		cell: null,
	})
	const highlight = { ...highlightWithoutEntry, entry: entries[highlightWithoutEntry.entryIndex] }

	const { gridStyle, people, griddedEntries } = useMemo(
		() => createCalendar(year, entries, holidays, teamMembers),
		[ year, entries, holidays, teamMembers ])

	const months = Info.months('numeric')
		// `month` is a string (yeah, I know)
		.map(month => parseInt(month))
	return (
		<div
			className={style.grid}
			style={{ ...gridStyle }}
			onMouseOver={event => updateHover(setHighlight, setHoveredEntry, event.target)}
			onClick={event => updateSelected(setSelectedEntry, event.target)}
			onMouseLeave={__ => updateHover(setHighlight, setHoveredEntry)}
		>
			{months.map(month => displayMonth(month, highlight))}
			{months.flatMap(month => people.map(person => displayPerson(month, person, highlight)))}
			{arrayTo(31).map(day => displayDayOfMonth(day, highlight))}
			{griddedEntries.map(entry => displayEntry(entry, people.map(person => person.abbreviation), highlight))}
		</div>
	)
}

const updateHover = (setHighlight, setHoveredEntry, cell) => {
	const determineHighlight = cell => {
		const updateToEntry = Boolean(cell?.dataset.entryindex)
		const updateToCell = cell && !cell.classList.contains(style.nonDay)

		if (updateToEntry) return { entryIndex: parseInt(cell.dataset.entryindex) }
		else if (updateToCell) return {
			cell: {
				month: parseInt(cell.dataset.month),
				day: parseInt(cell.dataset.day),
				person: parseInt(cell.dataset.person),
			}
		}
		else return {}
	}

	const highlight = determineHighlight(cell)
	setHighlight(highlight)
	setHoveredEntry(highlight?.entryIndex ?? -1)
}

const updateSelected = (setSelectedEntry, cell) => {
	setSelectedEntry(cell?.dataset.entryindex ?? -1)
}

/*
 * DISPLAY CALENDAR
 */

const displayMonth = (month, highlight) => {
	const abbreviation = Info.months('short')[month - 1]
	const name = Info.months('long')[month - 1]
	const className = `${style.month} ${(monthToHighlight(month, highlight) ? style.highlighted : "")}`
	const gridArea = abbreviation
	return (
		<div key={gridArea} className={className} style={{ gridArea }}>
			{name}
		</div>
	)
}

const monthToHighlight = (month, highlight) => {
	if (highlight.cell)
		return month === highlight.cell?.month
	if (highlight.entry)
		return computeEntrySplits(highlight.entry.start, highlight.entry.length)
			.map(monthInterval => monthInterval.start.month)
			.includes(month)
}

const displayPerson = (month, person, highlight) => {
	const monthAbbreviation = Info.months('short')[month - 1]
	const gridArea = `${monthAbbreviation}_${person.abbreviation}`
	// TODO use name
	const text = person.abbreviation === NO_PERSON ? "" : person.abbreviation
	const className = style.person + (personToHighlight(month, person, highlight) ? " " + style.highlighted : "")
	return (
		<div key={gridArea} className={className} style={{ gridArea }}>
			<div>{text}</div>
		</div>
	)
}
const personToHighlight = (month, person, highlight) => {
	if (!monthToHighlight(month, highlight)) return false
	if (highlight.cell) return person.indexInPeople === highlight.cell.person
	if (highlight.entry)
		return highlight.entry.people.length === 0
			? person.abbreviation === NO_PERSON
			: highlight.entry.people
				.map(person => person.abbreviation)
				.includes(person.abbreviation)
}


const displayDayOfMonth = (day, highlight) => {
	const className = style.dayOfMonth + (dayOfMonthToHighlight(day, highlight) ? " " + style.highlighted : "")
	return (
		<div key={day} className={className} style={{ gridArea: `d_${day + 1}` }}>
			{day + 1}
		</div>
	)
}

const dayOfMonthToHighlight = (day, highlight) => {
	if (highlight.cell) return day + 1 === highlight.cell?.day
	if (highlight.entry) {
		return arrayTo(highlight.entry.length)
			.map(day => highlight.entry.start.plus({ days: day }))
			.map(date => date.day)
			.includes(day + 1)
	}
}

const displayEntry = (entry, allPeople, highlight) => {
	const className = entry.className + " " + style.cell + " " + detectHighlightedEntryClass(entry, allPeople, highlight)
	const data = entry.data
		? {
			'data-month': entry.data.month,
			'data-day': entry.data.day,
			'data-person': entry.data.person,
			'data-entryindex': entry.data.entryIndex,
		}
		: {}
	return (
		<div
			key={entry.reactKey}
			className={className}
			style={{ ...entry.gridArea, backgroundColor: entry.color }}
			{...data}
		/>
	)
}

const detectHighlightedEntryClass = ({ data }, allPeople, highlight) => {
	if (highlight.cell) {
		const cell = highlight.cell
		if (data.day === cell.day &&
			(data.month < cell.month || (data.month === cell.month && data.person <= cell.person)))
			return style.highlightedRow
		if (data.month === cell.month && data.person === cell.person && data.day <= cell.day)
			return style.highlightedColumn
		return ""
	}

	if (highlight.entry) {
		if (data.entryIndex === highlight.entryIndex)
			return style.highlighted

		const classes = []
		const entry = highlight.entry
		const peopleIndices = entry.people
			.map(person => person.abbreviation)
			.map(abbreviation => allPeople.indexOf(abbreviation));
		const minPersonIndex = entry.people.length === 0
			? allPeople.length - 1
			: Math.min(...peopleIndices)
		const maxPersonIndex = entry.people.length === 0
			? allPeople.length - 1
			: Math.max(...peopleIndices)

		// highlight row
		const upTo = _date => data.day === _date.day &&
			(data.month < _date.month || (data.month === _date.month && data.person <= maxPersonIndex))
		if (upTo(entry.start)) {
			if (entry.length === 1) classes.push(style.highlightedRow)
			else classes.push(style.highlightedTop)
		} else if (upTo(entry.end) && entry.length > 1)
			classes.push(style.highlightedBottom)

		// highlight column
		if (data.month === entry.start.month && data.day < entry.start.day) {
			if (entry.people.length === 0) {
				if (data.person === maxPersonIndex) classes.push(style.highlightedColumn)
			} else if (entry.people.length === 1) {
				if (data.person === minPersonIndex) classes.push(style.highlightedColumn)
			} else {
				if (data.person === minPersonIndex) classes.push(style.highlightedLeft)
				if (data.person === maxPersonIndex) classes.push(style.highlightedRight)
			}
		}

		return classes.join(" ")
	}
}

/*
 * CREATE CALENDAR
 */

const createCalendar = (year, entries, holidays, people) => {
	const peopleWithUnknown = [ ...people, { name: "", abbreviation: NO_PERSON } ]
		.map((person, index) => ({ ...person, indexInPeople: index }))

	const { months, griddedEntries } = createEntries(entries, peopleWithUnknown)
	griddedEntries.unshift(...createCalendarStructure(holidays, year, months))

	return {
		gridStyle: computeGridStyle(months),
		people: peopleWithUnknown,
		griddedEntries,
	}
}

const createEntries = (entries, people) => {
	const griddedEntries = []
	const months = Info
		.months('short')
		.map(month => ({
			abbreviation: month,
			people: people.map(person => ({ ...person, columns: [ [] ] }))
		}))

	const processEntry = (person, entry, entryIndex, entrySplit) => {
		const month = months[entrySplit.start.month - 1]
		const monthPerson = month.people.find(p => p.abbreviation === (person?.abbreviation ?? NO_PERSON))
		const columnIndex = computeColumnIndex(monthPerson.columns, entrySplit)
		const griddedEntry = {
			color: entry.category.color,
			className: style.entry,
			gridArea: computeGridAreaFromInterval(monthPerson.abbreviation, columnIndex, entrySplit),
			reactKey: computeReactKeyFromInterval(monthPerson.abbreviation, columnIndex, entrySplit),
			data: { entryIndex }
		}
		if (columnIndex === monthPerson.columns.length) monthPerson.columns.push([])

		monthPerson.columns[columnIndex].push(entrySplit)
		griddedEntries.push(griddedEntry)
	}

	entries.forEach((entry, entryIndex) => {
		if (entry.people.length === 0)
			computeEntrySplits(entry.start, entry.length)
				.forEach(entrySplit => processEntry(null, entry, entryIndex, entrySplit))
		entry.people
			.forEach(person => computeEntrySplits(entry.start, entry.length)
				.forEach(entrySplit => processEntry(person, entry, entryIndex, entrySplit)))
	})

	return { months, griddedEntries }
}

const computeColumnIndex = (columns, entrySplit) => {
	for (let columnIndex = 0; columnIndex < columns.length; columnIndex++) {
		const fitsIntoColumn = !columns[columnIndex].find(split => split.overlaps(entrySplit))
		if (fitsIntoColumn) return columnIndex
	}

	return columns.length
}

const createCalendarStructure = (holidays, year, months) => {
	const processCalendarStructure = (year, month, day, person, columnSpan, holidays) => {
		const data = { month, day, person: person.indexInPeople }
		return {
			className: dayClassName(holidays, DateTime.local(year, month, day)),
			gridArea: computeGridArea(person.abbreviation, 0, month, day, columnSpan, 1),
			reactKey: computeReactKey(person.abbreviation, 0, `${month - 1}-${day}`),
			data
		}
	}

	return months
		.flatMap((month, monthIndex) => month
			.people
			.flatMap(person => arrayTo(31)
				.map(dayIndex => processCalendarStructure(
					year, monthIndex + 1, dayIndex + 1, person, person.columns.length, holidays))))
}

const dayClassName = (holidays, date) => {

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

const computeGridStyle = months => {
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

	return {
		// add first column for the days of the month
		gridTemplateColumns: `auto repeat(${totalColumns}, 1fr)`,
		gridTemplateAreas: `${monthRow} ${personRow} ${dayRows}`
	}
}

const arrayTo = length => [ ...Array(length).keys() ]

const computeEntrySplits = (start, length) => {
	const lastDayOfEachMonth = arrayTo(12)
		.map(index => ++index)
		.map(month => DateTime.local(start.year, month, 1))
	return Interval
		.after(start, Duration.fromObject({ days: length }))
		.splitAt(...lastDayOfEachMonth)
}

const computeGridAreaFromInterval = (person, columnIndex, interval) =>
	computeGridArea(person, columnIndex, interval.start.month, interval.start.day, 1, interval.length(`day`))

const computeGridArea = (person, columnIndex, month, day, columnSpan, rowSpan) => {
	const monthAbbreviation = Info.months('short')[month - 1]
	const column = `${monthAbbreviation}_${person}_c${columnIndex}`
	return {
		gridColumn: `${column}_d${day} / span ${columnSpan}`,
		gridRow: `${column}_d${day} / span ${rowSpan}`
	}
}

const computeReactKeyFromInterval = (person, columnIndex, interval) =>
	computeReactKey(person, columnIndex, interval.start.toFormat(`MM-dd`))

const computeReactKey = (person, columnIndex, date) =>
	`${person}_${columnIndex}_${date}`

export default Calendar
