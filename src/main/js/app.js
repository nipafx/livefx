import { useEffect, useState } from 'react'
import useWebSocket from 'react-use-websocket'

import Scene from "./scene";

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
	const [ miscTabInfo, setMiscTabInfo ] = useState({ active: "chat" })
	const [ topic, setTopic ] = useState("")
	const [ guests, setGuests ] = useState([])
	const [ schedule, setSchedule ] = useState([])
	const state = {
		miscTabInfo
	}
	const setState = {
		setLayout,
		setMessages,
		setTheme,
		setMiscTabInfo,
		setTopic,
		setGuests,
		setSchedule,
	}

	useEffect(() => {
		const unregisterSceneSetter = registerLayoutSetter(setLayout)
		return () => unregisterSceneSetter()
	}, [ command ])

	useEffect(() => {
		if (command) executeCommand(command, state, setState)
	}, [ command ])

	useEffect(() => {
		initializeLayout(setLayout)
		updateThemeColor(setState)
		updateTopic(setState)
		updateGuests(setState)
		updateMessages(setState)
		updateSchedule(setState)
	}, [])

	return (
		<Scene
			layout={layout}
			theme={theme}
			miscTabInfo={miscTabInfo}
			topic={topic}
			guests={guests}
			schedule={schedule}
			messages={messages}
		/>
	)
}

const initializeLayout = (setLayout) => {
	window?.obsstudio?.getCurrentScene(scene => setLayout(scene.name))
}

const registerLayoutSetter = (setLayout) => {
	const sceneSetter = event => setLayout(event.detail.name)
	window.addEventListener('obsSceneChanged', sceneSetter)
	return () => window.removeEventListener('obsSceneChanged', sceneSetter)
}

const executeCommand = (command, state, setState) => {
	console.log("Executing command", command)
	switch (command.type) {
		case "show-screen":
			showScreen()
			break
		case "show-tab":
			showTab(command.tab, command.info, state, setState)
			break
		case "update-messages":
			updateMessages(setState)
			break
		case "update-theme-color":
			updateThemeColor(setState)
			break
		case "update-topic":
			updateTopic(setState)
			break
		case "update-guests":
			updateGuests(setState)
			break
		default:
			// log unknown commands but do nothing else
			console.log("Unknown command", command)
	}
}

const showScreen = () => {
	window?.obsstudio?.setCurrentScene("screen, large cam")
}

const showTab = (tab, info, state, setState) => {
	const tabName = {
		DEFAULT: "chat",
		NOTES: "notes",
		SCHEDULE: "schedule"
	}[tab]
	state.miscTabInfo = {
		...state.miscTabInfo,
		active: tabName,
		[tabName]: info,
	}
	setState.setMiscTabInfo(state.miscTabInfo)
}

const updateMessages = (setState) => {
	update(`/api/messages?count=20`, response => response.messages, setState.setMessages)
}

const updateThemeColor = (setState) => {
	update(`/api/theme-color`, response => response.color.toLowerCase().replaceAll("_", "-"), setState.setTheme)
}

const updateGuests = (setState) => {
	update(`/api/guests`, response => response.guests, setState.setGuests)
}

const updateTopic = (setState) => {
	update(`/api/topic`, response => response.topic, setState.setTopic)
}

const updateSchedule = (setState) => {
	update(`/api/schedule`, response => response.schedule, setState.setSchedule)
}

const update = (endpoint, extract, set) => {
	fetch(endpoint)
		.then(response => response.json())
		.then(extract)
		.then(set)
}

export default App
