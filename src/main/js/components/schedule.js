import tabStyle from './tab.module.css'
import style from './schedule.module.css'
import { DateTime } from "luxon";

const Schedule = ({ schedule }) => (
	<div className={tabStyle.content}>
		<ul className={style.scheduleList}>
			{schedule.map(entry => (
				<li key={entry.startTime} className={style.entry}>
					<span className={style.time}>{DateTime
						.fromISO(entry.startTime, {setZone: true})
						.toFormat("cccc (dd.MM.), HHmm 'UTC'")}</span>
					<span className={style.title}>{entry.title}</span>
					<p className={style.description}>{entry.description}</p>
				</li>
			))}
		</ul>
	</div>
)

export default Schedule
