import React from 'react'

import style from './entryDetails.module.css'

const EntryDetails = ({ entries, hoveredEntry }) => {
	return (
		<div className={style.details}>
			{entries[hoveredEntry] ? displayEntry(entries[hoveredEntry]) : null}
		</div>
	)
}

const displayEntry = (entry) => {
	return <p>{entry.start}</p>
}

export default EntryDetails