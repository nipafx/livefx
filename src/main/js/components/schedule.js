import { DateTime, IANAZone } from "luxon";

import tabStyle from './tab.module.css'
import style from './schedule.module.css'

const Schedule = ({ schedule, timeZone }) => {
	let zone = new IANAZone(timeZone)
	if (!zone.isValid) zone = new IANAZone("UTC")
	return (
		<div className={tabStyle.content}>
			<p className={style.timeZone}>all times in {zone.name}</p>
			<ul className={style.scheduleList}>
				{schedule.map(entry => (
					<li key={entry.startTime} className={style.entry}>
					<span className={style.time}>{DateTime
						.fromISO(entry.startTime, {setZone: true})
						.setZone(zone)
						.toFormat("cccc (dd.MM.), HH:mm")}</span>
						<span className={style.title}>{entry.title}</span>
						<p className={style.description}>{entry.description}</p>
					</li>
				))}
			</ul>
		</div>
	)
}

export default Schedule
