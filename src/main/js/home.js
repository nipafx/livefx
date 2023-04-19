import { useEffect, useState } from 'react'
import useWebSocket from 'react-use-websocket'

import Layout from './layout/layout'
import Notes from './components/notes'
import Tab from './components/tab'
import Window from './components/window'

import style from './home.module.css'
import DebugInfo from "./components/debugInfo";

const config = {
	"debug": true,
	"stream": "reboot",
}

const LAYOUTS = [ "cam, screen", "screen, large cam", "screen, small cam", "screen, small cam, guest2" ]
const THEMES = [ "green", "red", "orange", "yellow", "blue-light", "blue-dark", "purple", "pink" ]

const Home = () => {
	const { lastJsonMessage: command, readyState: commandState } = useWebSocket("ws://localhost:8080/command")
	const [ layout, setLayout ] = useState(LAYOUTS[0])
	const nextLayout = () => LAYOUTS[(LAYOUTS.indexOf(layout) + 1) % LAYOUTS.length]
	const [ theme, setTheme ] = useState(THEMES[0])
	const nextTheme = () => THEMES[(THEMES.indexOf(theme) + 1) % THEMES.length]

	useEffect(() => {
		if (command) executeCommand(command, setLayout, setTheme)
		const unregisterSceneSetter = registerLayoutSetter(setLayout)
		return () => unregisterSceneSetter()
	})

	const debug = config?.debug
	const guest = config?.guest
	const guest2 = config?.guest2

	const layoutClasses = determineLayoutClasses(layout)
	const classes = [ "theme-" + theme, ...layoutClasses ]

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
				{debug && <Tab name="debug">
					<DebugInfo
						layout={layout} nextLayout={nextLayout}
						theme={theme} nextTheme={nextTheme}
						command={command} commandState={commandState}
					/>
				</Tab>}
				<Tab name="notes">
					<Notes stream={config.stream} />
				</Tab>
				<Tab name="chat" />
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

const registerLayoutSetter = (setLayout) => {
	const sceneSetter = event => setLayout(event.detail.name)
	window.addEventListener('obsSceneChanged', sceneSetter)
	return () => window.removeEventListener('obsSceneChanged', sceneSetter)
}

const executeCommand = (command, setLayout, setTheme) => {
	console.log("Executing command", command)
	switch (command.type) {
		case "change-theme-color":
			setThemeColor(command.newColor, setTheme)
			break
	}
}

const setThemeColor = (newColor, setTheme) => {
	const themeName = "theme-" + newColor.toLowerCase().replaceAll("_", "-")
	setTheme(themeName)
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

export default Home
