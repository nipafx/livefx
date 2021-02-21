import React from 'react'

import style from './entryDetails.module.css'
import { DateTime } from "luxon";

const EntryDetails = ({ entries, holidays, people, entryIndex }) => {
	const entry = entries[entryIndex]
	return (
		<div>
			<div className={style.header}>Details</div>
			<div className={style.details}>
				<p key="date">
					<span className={style.type}>Date: </span>
					{displayDate(entry)}
				</p>
				<p key="category">
					<span className={style.type}>Category: </span>
					{entry?.category?.name}
				</p>
				<p key="people">
					<span className={style.type}>People: </span>
					{entry?.people?.map(person =>
						<span key={person.name} className={style.person}>{person.name}</span>)}
				</p>
				<p key="description">
					<span className={style.type}>Description: </span>
					{entry?.description}
				</p>
			</div>
		</div>
	)
}

const displayDate = entry => {
	if (!entry) return
	const start = DateTime.fromISO(entry.start).toFormat('dd.MM.')
	const end = entry.length > 1
		? " - " + DateTime.fromISO(entry.end).toFormat('dd.MM.')
		: ""
	return start + end
}

export default EntryDetails