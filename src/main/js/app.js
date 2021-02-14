import React, { useEffect, useState } from 'react'

import { DateTime } from 'luxon'

import Calendar from './calendar'

import './global.css'
import style from './app.module.css'

const App = () => {

	const year = DateTime.local().year
	const [ calendar, setCalendar ] = useState({
		entries: [],
		holidays: [],
		people: [],
	})

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
				.then(([ entries, holidays, people ]) =>
					setCalendar({ entries, holidays, people }))
		}, [ year ])

	return (
		<div className={style.app}>
			<Calendar year={year} {...calendar}/>
		</div>
	)
}

export default App
