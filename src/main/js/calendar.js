import React, { useEffect, useState } from 'react';
import { DateTime, Info } from 'luxon';

import style from './calendar.module.css';

const Calendar = () => {

	const [ calendar, setCalendar ] = useState({ gridTemplateAreas: "", persons: [], entries: [] })

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
		<div className={style.grid} style={{ gridTemplateAreas: calendar.gridTemplateAreas }}>
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
	return (
		<div key={gridArea} className={style.person} style={{ gridArea }}>
			{/* TODO use name */}
			<div>{person.abbreviation}</div>
		</div>
	)
}

const displayDayOfMonth = day => (
	<div key={day} className={style.dayOfMonth} style={{ gridArea: `d_${day + 1}` }}>
		{day + 1}
	</div>
)

const displayEntry = entry => (
	<div key={entry.gridArea} style={{ gridArea: entry.gridArea, backgroundColor: entry.category.color }}/>
)

const createCalendar = (entries, persons) => {
	const griddedEntries = []
	const months = Info
		.months('short')
		.map(month => ({
			abbreviation: month,
			persons: persons.map(person => ({ ...person, columns: [ [] ] }))
		}))

	entries.forEach(entry => {
		const start = DateTime.fromISO(entry.start)
		entry.persons.forEach(_person => {
			const month = months[start.month - 1]
			const person = month.persons.find(person => person.abbreviation === _person.abbreviation)
			const columnIndex = computeColumnIndex(months, entry)
			const day = start.day
			const gridArea = computeGridArea(month, person, columnIndex, day)
			const griddedEntry = { start, person: _person, category: entry.category, gridArea }

			person.columns[columnIndex].push(griddedEntry)
			griddedEntries.push(griddedEntry)
		})
	})

	return {
		gridTemplateAreas: computeGridTemplateAreas(months),
		persons,
		entries: griddedEntries
	}
}

const computeColumnIndex = (months, entry) => {
	// TODO assuming one column per person is enough
	return 0
}

const computeGridTemplateAreas = months => {
	const monthRowRaw = months
		.flatMap(month => month
			.persons
			.flatMap(person => arrayTo(person.columns.length)
				.map(_ => `${month.abbreviation}`)
			))
		.join(" ")
	// to make sure numbers of columns align, add one for the days of the month
	const monthRow = `' . ${monthRowRaw}'`

	const personRowRaw = months
		.flatMap(month => month
			.persons
			.flatMap(person => arrayTo(person.columns.length)
				.map(_ => `${month.abbreviation}_${person.abbreviation}`)
			))
		.join(" ")
	// to make sure numbers of columns align, add one for the days of the month
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

	return `${monthRow} ${personRow} ${dayRows}`
}

const arrayTo = length => [ ...Array(length).keys() ]

const computeGridArea = (month, person, columnIndex, day) =>
	`${month.abbreviation}_${person.abbreviation}_c${columnIndex}_d${day}`;

export default Calendar;
