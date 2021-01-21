import React, { useEffect, useState } from 'react';
import { DateTime, Duration, Info, Interval } from 'luxon';

import style from './calendar.module.css';

const NO_PERSON = "NONE";

const Calendar = () => {

	const [ calendar, setCalendar ] = useState({
		gridStyle: { gridTemplateColumns: "", gridTemplateAreas: "" },
		persons: [],
		entries: []
	})

	useEffect(() => {
		Promise
			.all([
				fetch('/api/entry')
					.then(response => response.text())
					.then(entriesString => JSON.parse(entriesString)),
				fetch('/api/person')
					.then(response => response.text())
					.then(personString => JSON.parse(personString)) ])
			.then(([ entries, persons ]) => createCalendar(entries, persons))
			.then(setCalendar)
	}, [])

	return (
		<div className={style.grid} style={{ ...calendar.gridStyle }}>
			{Info.months('numeric').map(displayMonth)}
			{Info.months('short')
				.flatMap(monthAbbreviation => calendar
					.persons
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
	<div key={entry.reactKey} style={{ ...entry.gridArea, backgroundColor: entry.category.color }}/>
)

const createCalendar = (entries, persons) => {
	const personsWithUnknown = [ ...persons, { name: "", abbreviation: NO_PERSON } ]

	const griddedEntries = []
	const months = Info
		.months('short')
		.map(month => ({
			abbreviation: month,
			persons: personsWithUnknown.map(person => ({ ...person, columns: [ [] ] }))
		}))

	const process = (person, entry, entrySplit) => {
		const month = months[entrySplit.start.month - 1]
		const monthPerson = month.persons.find(p => p.abbreviation === (person?.abbreviation ?? NO_PERSON))
		const columnIndex = computeColumnIndex(monthPerson.columns, entrySplit)
		const gridArea = computeGridArea(monthPerson.abbreviation, columnIndex, entrySplit)
		const reactKey = computeReactKey(monthPerson.abbreviation, columnIndex, entrySplit)
		const griddedEntry = { person, category: entry.category, gridArea, reactKey }
		if (columnIndex === monthPerson.columns.length) monthPerson.columns.push([])
		monthPerson.columns[columnIndex].push(entrySplit)
		griddedEntries.push(griddedEntry)
	}

	entries.forEach(entry => {
		const start = DateTime.fromISO(entry.start)
		if (entry.persons.length === 0)
			computeEntrySplits(start, entry.length)
				.forEach(entrySplit => process(null, entry, entrySplit))
		entry.persons
			.forEach(person => computeEntrySplits(start, entry.length)
				.forEach(entrySplit => process(person, entry, entrySplit)))
	})

	return {
		gridStyle: computeGridStyle(months),
		persons: personsWithUnknown,
		entries: griddedEntries
	}
}

const computeColumnIndex = (columns, entrySplit) => {
	for (let columnIndex = 0; columnIndex < columns.length; columnIndex++) {
		const fitsIntoColumn = !columns[columnIndex].find(split => split.overlaps(entrySplit))
		if (fitsIntoColumn) return columnIndex
	}

	return columns.length
}

const computeGridStyle = months => {
	const totalColumns = months
		.flatMap(month => month.persons.map(person => person.columns.length))
		.reduce((result, columns) => result + columns, 0)

	const monthRowRaw = months
		.flatMap(month => month
			.persons
			.flatMap(person => arrayTo(person.columns.length)
				.map(_ => `${month.abbreviation}`)
			))
		.join(" ")
	// add first column for the days of the month
	const monthRow = `' . ${monthRowRaw}'`

	const personRowRaw = months
		.flatMap(month => month
			.persons
			.flatMap(person => arrayTo(person.columns.length)
				.map(_ => `${month.abbreviation}_${person.abbreviation}`)
			))
		.join(" ")
	// add first column for the days of the month
	const personRow = `' . ${personRowRaw}'`

	const dayRows = arrayTo(31)
		.map(day => {
			const dayRow = months
				.flatMap(month => month.persons
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

const computeGridArea = (person, columnIndex, interval) => {
	const length = interval.length(`day`)
	const month = Info.months('short')[interval.start.month - 1]
	const column = `${month}_${person}_c${columnIndex}`
	return {
		gridColumn: `${column}_d${interval.start.day}`,
		gridRow: `${column}_d${interval.start.day} / span ${length}`
	}
}

const computeReactKey = (person, columnIndex, interval) =>
	`${person}_${columnIndex}_${interval.start.toFormat(`MM-dd`)}`

export default Calendar;
