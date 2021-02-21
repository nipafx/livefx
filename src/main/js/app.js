import React, { useEffect, useState } from 'react'

import { DateTime } from 'luxon'

import Calendar from './calendar'
import EntryDetails from "./entryDetails"

import './global.css'
import style from './app.module.css'

const App = () => {

	const year = DateTime.local().year
	const [ calendar, setCalendar ] = useState({
		entries: [],
		holidays: [],
		people: [],
	})
	const [ hoveredEntry, setHoveredEntry ] = useState(-1)
	const [ selectedEntry, setSelectedEntry ] = useState(-1)

	useEffect(
		() => {
			Promise
				.all([
					fetch(`/api/entries?year=${year}`)
						.then(response => response.text())
						.then(entriesString => JSON.parse(entriesString)),
					fetch(`/api/holidays?year=${year}`)
						.then(response => response.text())
						.then(holidayString => JSON.parse(holidayString)),
					fetch(`/api/people`)
						.then(response => response.text())
						.then(personString => JSON.parse(personString)) ])
				.then(([ entries, holidays, people ]) => {
					const entriesWithParsedDates = entries
						.map(entry => ({
							...entry,
							start: DateTime.fromISO(entry.start),
							end: DateTime.fromISO(entry.start).plus({ days: entry.length - 1 }),
						}))
					const holidaysWithParsedDates = holidays
						.map(holiday => ({ ...holiday, date: DateTime.fromISO(holiday.date) }))
					setCalendar({
						entries: entriesWithParsedDates,
						holidays: holidaysWithParsedDates,
						people
					});
				})
		}, [ year ])

	return (
		<div className={style.app}>
			<div className={style.calendar}>
				<Calendar
					year={year}
					{...calendar}
					setHoveredEntry={setHoveredEntry}
					setSelectedEntry={setSelectedEntry}/>
			</div>
			<div className={style.sidebar}>
				<EntryDetails key="hovered-details" {...calendar} entryIndex={hoveredEntry}/>
				<EntryDetails key="selected-details" {...calendar} entryIndex={selectedEntry}/>
			</div>
		</div>
	)
}

export default App
