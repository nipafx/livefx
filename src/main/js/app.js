import { useEffect, useState } from 'react'
import useWebSocket from 'react-use-websocket'

import Scene from "./scene";
import DebugInfo from "./components/debugInfo";

const config = {
	"debug": true,
	"stream": "reboot",
}

const LAYOUTS = [ "cam, screen", "screen, large cam", "screen, small cam", "screen, small cam, guest2" ]
const THEMES = [ "green", "red", "orange", "yellow", "blue-light", "blue-dark", "purple", "pink" ]

const App = () => {
	const {
		sendMessage,
		lastJsonMessage: command,
		readyState: commandState
	} = useWebSocket("ws://localhost:8080/command")
	const [ layout, setLayout ] = useState(LAYOUTS[0])
	const [ theme, setTheme ] = useState(THEMES[0])

	useEffect(() => {
		if (command) executeCommand(command, setLayout, setTheme)
		const unregisterSceneSetter = registerLayoutSetter(setLayout)
		return () => unregisterSceneSetter()
	})

	const debug = config?.debug
	const guest = config?.guest
	const guest2 = config?.guest2

	return (
		<Scene layout={layout} theme={theme} stream={config.stream}>
			{debug && <DebugInfo
				layout={layout} triggerNextLayout={() => triggerNextLayout(layout)}
				theme={theme} triggerNextTheme={() => triggerNextTheme(sendMessage, theme)}
				command={command} commandState={commandState}
			/>}
		</Scene>
	)
}

const registerLayoutSetter = (setLayout) => {
	const sceneSetter = event => setLayout(event.detail.name)
	window.addEventListener('obsSceneChanged', sceneSetter)
	return () => window.removeEventListener('obsSceneChanged', sceneSetter)
}

const triggerNextLayout = (layout) => {
	const nextLayout = LAYOUTS[(LAYOUTS.indexOf(layout) + 1) % LAYOUTS.length]
	const event = new CustomEvent(
		'obsSceneChanged',
		{ detail: { name: nextLayout } }
	)
	window.dispatchEvent(event)
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
	const themeName = newColor.toLowerCase().replaceAll("_", "-")
	setTheme(themeName)
}

const triggerNextTheme = (sendMessage, theme) => {
	const nextTheme = THEMES[(THEMES.indexOf(theme) + 1) % THEMES.length]
	const nextThemeCommand = {
		type: "change-theme-color",
		newColor: nextTheme,
	}
	sendMessage("ECHO " + JSON.stringify(nextThemeCommand))
}

export default App
