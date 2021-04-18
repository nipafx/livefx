import React, { useEffect, useState } from 'react'

import { DateTime } from 'luxon'

import { Category, Entry, Holiday, Person, ThemedYear } from "./types";

import Calendar from './calendar'
import Sidebar from "./sidebar";

import './global.css'

const style = require('./app.module.css')

const App = () => {

	const year: number = DateTime.local().year
	const emptyState: State = {
		entries: [],
		holidays: [],
		people: [],
		categories: [],
		themes: [],
	}
	const [ calendar, setCalendar ] = useState(emptyState)
	const [ hoveredEntry, setHoveredEntry ] = useState(undefined as Entry | undefined)
	const [ selectedEntry, setSelectedEntry ] = useState(undefined as Entry | undefined)

	useEffect(
		() => {
			Promise
				.all([
					fetch(`/api/entries?year=${year}`)
						.then(response => response.text())
						.then(entriesString => JSON.parse(entriesString) as EntryResponse[]),
					fetch(`/api/holidays?year=${year}`)
						.then(response => response.text())
						.then(holidayString => JSON.parse(holidayString)),
					fetch(`/api/people`)
						.then(response => response.text())
						.then(personString => JSON.parse(personString)),
					fetch(`/api/categories`)
						.then(response => response.text())
						.then(categoriesString => JSON.parse(categoriesString)),
					fetch(`/api/themes?year=${year}`)
						.then(response => response.text())
						.then(themesString => JSON.parse(themesString)) ])
				.then(([ entries, holidays, people, categories, themes ]) => {
					setCalendar({
						entries: entries.map(responseToEntry),
						holidays: holidays.map(responseToHoliday),
						people,
						categories,
						themes
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
			<Sidebar {...calendar} hoveredEntry={hoveredEntry} selectedEntry={selectedEntry}/>
		</div>
	)
}

interface State {
	entries: Entry[]
	holidays: Holiday[]
	people: Person[]
	categories: Category[]
	themes: ThemedYear[]
}

interface EntryResponse {
	category: Category
	description: string
	people: Person[]

	start: string
	length: number
}

const responseToEntry = (entry: EntryResponse): Entry => ({
	...entry,
	start: DateTime.fromISO(entry.start),
	end: DateTime.fromISO(entry.start).plus({ days: entry.length - 1 }),
})

interface HolidayResponse {
	name: string
	date: string
}

const responseToHoliday = (holiday: HolidayResponse): Holiday => ({
	...holiday,
	date: DateTime.fromISO(holiday.date)
})

export default App
