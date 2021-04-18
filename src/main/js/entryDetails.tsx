import React from 'react'

import { Entry } from "./types"

const style = require('./entryDetails.module.css')
const sidebarStyle = require('./sidebar.module.css')

interface EntryDetailsProperties {
	entry?: Entry
}

const EntryDetails = ({ entry }: EntryDetailsProperties) => {
	return (
		<div>
			<div className={sidebarStyle.header}>Details</div>
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

const displayDate = (entry?: Entry): string => {
	if (!entry) return ""
	const start = entry.start.toFormat('dd.MM.')
	const end = entry.length > 1
		? " - " + entry.end.toFormat('dd.MM.')
		: ""
	return start + end
}

export default EntryDetails
