import Layout from './layout/layout'
import Notes from './components/notes'
import Tab from './components/tab'
import Window from './components/window'

import style from './scene.module.css'
import Chat from "./components/chat";

const Scene = ({ layout, theme, stream, guest, guest2, messages, children }) => {
	const layoutClasses = determineLayoutClasses(layout)
	const classes = [ ...layoutClasses, "theme-" + theme ]
	children = Array.isArray(children) ? children : [ children ]

	return (
		<Layout id={style.root} className={classes}>
			<Window name="screens" className={style.screen}>
				{guest && [ "cam, screen", "guest, large cam" ].includes(layout) && <Tab name={guest} />}
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
			<Window name="misc" className={style.misc}>
				{guest && layout.startsWith("screen, ") && <Tab name={guest} />}
				{guest2 && layout.startsWith("cam, ") && <Tab name={guest2} />}
				<Tab name="chat" >
					<Chat messages={messages} />
				</Tab>
				<Tab name="notes">
					<Notes stream={stream} />
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
