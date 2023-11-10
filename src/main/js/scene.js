import Layout from './layout/layout'
import Notes from './components/notes'
import Tab from './components/tab'
import Window from './components/window'

import style from './scene.module.css'
import Chat from "./components/chat";
import Schedule from "./components/schedule";

const Scene = ({ layout, theme, miscTabInfo, topic, guests, schedule, messages, children }) => {
	const layoutClasses = determineLayoutClasses(layout)
	const classes = [ ...layoutClasses, "theme-" + theme ]
	children = Array.isArray(children) ? children : [ children ]

	const guest1 = guests && guests[0]?.name
	const guest2 = guests && guests[1]?.name

	return (
		<Layout id={style.root} className={classes}>
			<Window name="screens" className={style.screen}>
				{guest1 && [ "cam, screen", "guest, large cam" ].includes(layout) && <Tab name={guest1} />}
				<Tab name="screen #1" />
				<Tab name="screen #2" />
			</Window>
			<Window name="cam" className={style.camera}>
				<Tab name="cam" />
			</Window>
			{guest2 && layout.startsWith("screen, ") && layout.includes("guest2") && (
				<Window name="guest2" className={style.guest2}>
					<Tab name={guest2} />
				</Window>
			)}
			<Window name="misc" activeTab={miscTabInfo.active} className={style.misc}>
				{guest1 && layout.startsWith("screen, ") && <Tab name={guest1} />}
				{guest2 && layout.startsWith("cam, ") && <Tab name={guest2} />}
				<Tab name="chat">
					<Chat messages={messages} info={miscTabInfo.chat}/>
				</Tab>
				<Tab name="notes">
					<Notes topic={topic} info={miscTabInfo.notes} />
				</Tab>
				<Tab name="schedule">
					<Schedule schedule={schedule} timeZone={miscTabInfo.schedule} />
				</Tab>
				{children}
			</Window>
			<div id={style.filler1} className={style.filler} />
			<div id={style.filler2} className={style.filler} />
			<div id={style.filler3} className={style.filler} />
			<div id={style.filler4} className={style.filler} />
			<div id={style.filler5} className={style.filler} />
			<div id={style.filler6} className={style.filler} />
			<div id={style.filler7} className={style.filler} />
		</Layout>
	)
}

const determineLayoutClasses = name => {
	switch (name) {
		case "cam, screen":
			return [ style.largeCamLeft, style.largeScreenRight ]
		case "screen, large cam":
		case "guest, large cam":
			return [ style.largeScreenLeft, style.largeCamRight ]
		case "screen, small cam":
			return [ style.largeScreenLeft, style.smallCamRight ]
		case "screen, small cam, guest2":
			return [ style.largeScreenLeft, style.smallCamRight, style.smallGuestRight ]
		default:
			return [ style.hidden ]
	}
}

export default Scene
