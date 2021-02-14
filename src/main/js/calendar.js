import React, { useEffect, useState } from 'react';
import { DateTime, Duration, Info, Interval } from 'luxon';

import style from './calendar.module.css';

const NO_PERSON = "NONE";

const Calendar = () => {

	const [ calendar, setCalendar ] = useState({
		gridStyle: { gridTemplateColumns: "", gridTemplateAreas: "" },
		people: [],
		entries: [],
		year: DateTime.local().year
	})

	useEffect(
		() => {
			Promise
				.all([
					fetch(`/api/entries?year=${calendar.year}`)
						.then(response => response.text())
						.then(entriesString => JSON.parse(entriesString)),
					fetch(`/api/holidays?year=${calendar.year}`)
						.then(response => response.text())
						.then(holidayString => JSON.parse(holidayString)),
					fetch(`/api/people`)
						.then(response => response.text())
						.then(personString => JSON.parse(personString)) ])
				.then(([ entries, holidays, people ]) =>
					createCalendar(calendar.year, entries, holidays, people))
				.then(setCalendar)
		},
		[ calendar.year ])

	return (
		<div className={style.grid} style={{ ...calendar.gridStyle }}>
			{Info.months('numeric').map(displayMonth)}
			{Info.months('short')
				.flatMap(monthAbbreviation => calendar
					.people
					.map(person => displayPerson(monthAbbreviation, person)))}
			{arrayTo(31).map(displayDayOfMonth)}
			{calendar.entries.map(displayEntry)}
		</div>
	)
}

const displayMonth = month => {
	const abbreviation = Info.months('short')[month - 1]
	const name = Info.months('long')[month - 1]
	const gridArea = abbreviation
	return (
		<div key={gridArea} className={style.month} style={{ gridArea }}>
			{name}
		</div>
	)
}

const displayPerson = (monthAbbreviation, person) => {
	const gridArea = `${monthAbbreviation}_${person.abbreviation}`
	// TODO use name
	const text = person.abbreviation === NO_PERSON ? "" : person.abbreviation;
	return (
		<div key={gridArea} className={style.person} style={{ gridArea }}>
			<div>{text}</div>
		</div>
	)
}

const displayDayOfMonth = day => (
	<div key={day} className={style.dayOfMonth} style={{ gridArea: `d_${day + 1}` }}>
		{day + 1}
	</div>
)

const displayEntry = entry => (
	<div key={entry.reactKey} className={entry.className} style={{ ...entry.gridArea, backgroundColor: entry.category.color }}/>
)

const createCalendar = (year, entries, holidays, people) => {
	const peopleWithUnknown = [ ...people, { name: "", abbreviation: NO_PERSON } ]

	const griddedEntries = []
	const months = Info
		.months('short')
		.map(month => ({
			abbreviation: month,
			people: peopleWithUnknown.map(person => ({ ...person, columns: [ [] ] }))
		}))

	// `createEntries` modifies `months` and `griddedEntries` and `createCalendarStructure`
	// depends on that, so don't reorder these methods
	createEntries(entries, months, griddedEntries);
	createCalendarStructure(holidays, year, months, griddedEntries);

	return {
		gridStyle: computeGridStyle(months),
		people: peopleWithUnknown,
		entries: griddedEntries,
		year
	}
}

function createEntries(entries, months, griddedEntries) {
	const processEntry = (person, entry, entrySplit) => {
		const month = months[entrySplit.start.month - 1]
		const monthPerson = month.people.find(p => p.abbreviation === (person?.abbreviation ?? NO_PERSON))
		const columnIndex = computeColumnIndex(monthPerson.columns, entrySplit)
		const gridArea = computeGridAreaFromInterval(monthPerson.abbreviation, columnIndex, entrySplit)
		const reactKey = computeReactKeyFromInterval(monthPerson.abbreviation, columnIndex, entrySplit)
		const griddedEntry = { person, category: entry.category, className: style.entry, gridArea, reactKey }
		if (columnIndex === monthPerson.columns.length) monthPerson.columns.push([])
		monthPerson.columns[columnIndex].push(entrySplit)
		griddedEntries.push(griddedEntry)
	}

	entries.forEach(entry => {
		const start = DateTime.fromISO(entry.start)
		if (entry.people.length === 0)
			computeEntrySplits(start, entry.length)
				.forEach(entrySplit => processEntry(null, entry, entrySplit))
		entry.people
			.forEach(person => computeEntrySplits(start, entry.length)
				.forEach(entrySplit => processEntry(person, entry, entrySplit)))
	})
}

const computeColumnIndex = (columns, entrySplit) => {
	for (let columnIndex = 0; columnIndex < columns.length; columnIndex++) {
		const fitsIntoColumn = !columns[columnIndex].find(split => split.overlaps(entrySplit))
		if (fitsIntoColumn) return columnIndex
	}

	return columns.length
}

function createCalendarStructure(holidays, year, months, griddedEntries) {
	const processCalendarStructure = (year, month, day, person, columnSpan, holidays) => {
		const category = dayCategory(holidays, DateTime.local(year, month, day))
		if (category) {
			const gridArea = computeGridArea(person.abbreviation, 0, month, day, columnSpan, 1)
			const reactKey = computeReactKey(person.abbreviation, 0, `${month - 1}-${day}`)
			const griddedEntry = { person, category: category, className: category.className, gridArea, reactKey }
			griddedEntries.unshift(griddedEntry)
		}
	}

	months
		.flatMap((month, monthIndex) => month
			.people
			.flatMap(person => arrayTo(31)
					.forEach(dayIndex => processCalendarStructure(
						year, monthIndex + 1, dayIndex + 1, person, person.columns.length, holidays))))
}

const dayCategory = (holidays, date) => {

	// the order of the following checks is important:
	// only one div will be created per grid cell, so later types of divs (e.g. for weekends)
	// won't be created if an earlier check was true (e.g. holidays)

	if (!date.isValid)
		return {
			name: "not-a-day",
			abbreviation: "nad",
			className: style.nonDay
		}

	const isHoliday = holidays
		.map(holiday => DateTime.fromISO(holiday.date))
		.some(holiday => holiday.equals(date))
	if (isHoliday)
		return {
			name: "holiday",
			abbreviation: "hds",
			className: style.holiday
		}

	const isWeekendDay = date.weekday === 6 || date.weekday === 7;
	if (isWeekendDay)
		return {
			name: "weekend",
			abbreviation: "wkd",
			className: style.weekendDay
		}

	return {
		name: "empty",
		abbreviation: "emy",
		className: style.emptyDay
	}
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

export default Calendar;
