import { useEffect, useState } from 'react'
import useWebSocket from 'react-use-websocket'

import Scene from "./scene";

const config = {
	"stream": "reboot",
}

const LAYOUTS = [ "cam, screen", "screen, large cam", "screen, small cam", "screen, small cam, guest2" ]
const THEMES = [ "green", "red", "orange", "yellow", "blue-light", "blue-dark", "purple", "pink" ]

const App = () => {
	const {
		lastJsonMessage: command,
	} = useWebSocket(
		"ws://localhost:8080/command",
		{
			retryOnError: true,
			shouldReconnect: (closeEvent) => true,
			reconnectAttempts: 1_000_000,
			reconnectInterval: 1000,
		})
	const [ layout, setLayout ] = useState(LAYOUTS[0])
	const [ messages, setMessages ] = useState([])
	const [ theme, setTheme ] = useState(THEMES[0])
	const [ guests, setGuests ] = useState([])

	useEffect(() => {
		const unregisterSceneSetter = registerLayoutSetter(setLayout)
		return () => unregisterSceneSetter()
	}, [ command ])

	useEffect(() => {
		if (command) executeCommand(command, setLayout, setMessages, setTheme)
	}, [ command ])

	useEffect(() => {
		updateThemeColor(setTheme)
	}, [])

	useEffect(() => {
		updateGuests(setGuests)
	}, [])

	return (
		<Scene layout={layout} theme={theme} stream={config.stream} guests={guests} messages={messages} />
	)
}

const registerLayoutSetter = (setLayout) => {
	const sceneSetter = event => setLayout(event.detail.name)
	window.addEventListener('obsSceneChanged', sceneSetter)
	return () => window.removeEventListener('obsSceneChanged', sceneSetter)
}

const executeCommand = (command, setLayout, setMessages, setTheme) => {
	console.log("Executing command", command)
	switch (command.type) {
		case "update-messages":
			updateMessages(setMessages)
			break
		case "update-theme-color":
			updateThemeColor(setTheme)
			break
		default:
			// log unknown commands but do nothing else
			console.log("Unknown command", command)
	}
}

const updateMessages = (setMessages) => {
	fetch(`/api/messages?count=20`)
		.then(response => response.json())
		.then(setMessages)
}

const updateThemeColor = (setTheme) => {
	fetch(`/api/theme-color`)
		.then(response => response.json())
		.then(color => color.toLowerCase().replaceAll("_", "-"))
		.then(setTheme)
}

const updateGuests = (setGuests) => {
	fetch(`/api/guests`)
		.then(response => response.json())
		.then(setGuests)
}

export default App
