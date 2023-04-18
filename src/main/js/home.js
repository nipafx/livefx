import { useEffect, useState } from 'react'
import useWebSocket, { ReadyState } from 'react-use-websocket'

import Layout from './layout/layout'
import Notes from './components/notes'
import Tab from './components/tab'
import Window from './components/window'

import style from './home.module.css'

// TODO
const config = {
	"stream": "reboot",
}

const LAYOUTS = ["cam, screen", "screen, large cam", "screen, small cam", "screen, small cam, guest2"]
const THEMES = [ "theme-green", "theme-red", "theme-orange", "theme-yellow", "theme-blue-light", "theme-blue-dark", "theme-purple", "theme-pink" ]

const Home = () => {
	const { sendMessage: _, lastMessage: command, readyState: commandState } = useWebSocket("ws://localhost:8080/command")
	const [ layout, setLayout ] = useState(LAYOUTS[0])
	const [ theme, setTheme ] = useState(THEMES[0])

	useEffect(() => {
		if (command?.data) executeCommand(JSON.parse(command.data), setLayout, setTheme)
		const unregisterSceneSetter = registerLayoutSetter(setLayout)
		return () => unregisterSceneSetter()
	})

	const debug = config?.debug
	const guest = config?.guest
	const guest2 = config?.guest2
	const layoutClasses = determineLayoutClasses(layout)
	const classes = [ theme, ...layoutClasses ]

	return (
		<Layout id={style.root} className={classes}>
			<Window name="screens" className={style.screen}>
				{guest && ["cam, screen", "guest, large cam"].includes(layout) && <Tab name={guest} />}
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
				<Tab name="notes">
					<Notes stream={config.stream}/>
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
			{debug &&
				<button
					style={{height: "50px", width: "50px", zIndex: 5, position: "absolute", bottom: 0, right: 0}}
					onClick={event => window.dispatchEvent(
						new CustomEvent(
							'obsSceneChanged',
							{ detail: {name: LAYOUTS[(LAYOUTS.indexOf(layout) + 1) % LAYOUTS.length] }}))} />
			}
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
