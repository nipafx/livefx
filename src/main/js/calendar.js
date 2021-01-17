import React, { useEffect, useState } from 'react';

import style from './calendar.module.css';

const Calendar = () => {

	const [ entries, setEntries ] = useState([])

	useEffect(() => {
		const fetchEntries = async () => {
			const response = await fetch('/api/entry')
			const entriesString = await response.text()
			return JSON.parse(entriesString)
		}
		fetchEntries().then(setEntries)
	}, [])

	return (
		<div className={style.calendar}>
			<header className={style.calendarHeader}>
				<ul>
					{entries.map(displayEntry)}
				</ul>
			</header>
		</div>
	)
}

const displayEntry = entry => <li key={entry.start}>{entry.start}</li>

export default Calendar;
