import tabStyle from './tab.module.css'

const Notes = ({ stream }) => (
	<div className={tabStyle.content}>
		{showNotesFor(stream)}
	</div>
)

const showNotesFor = stream => {
	switch (stream) {
		case "junit-pioneer": return notesJUnitPioneer()
		case "module-woes": return notesModuleWoes()
		case "java-after-11": return notesJavaAfterEleven()
		case "livefx": return notesLiveFx()
		case "loom": return notesLoom()
		case "java-map": return notesJavaMap()
		case "java-x-demo": return notesJavaXDemo()
		case "java-18": return notesJava18()
		case "jep-431": return notesJep431()
		case "reboot": return notesReboot()
		default: throw new Error("Unknown stream: " + stream)
	}
}

// Maintenance of JUnit Pioneer, a JUnit 5 extension pack #Java
// You. Us. JUnit Pioneer. Now.
const notesJUnitPioneer = () => (
	// <>
	// 	<h1>JUnit Pioneer</h1>
	// 	<p>JUnit Pioneer is a JUnit 5 extension pack, collecting all kinds of useful functionality that isn't big enough for its own project.</p>
	// 	<p className={tabStyle.indent}>https://junit-pioneer.org</p>
	// 	<p>This is us maintaining it together. We're using a Kanban-tabStyle board to manage what to do next:</p>
	// 	<p className={tabStyle.indent}>https://bit.ly/exploring-io</p>
	// </>
	<>
		<h1>JUnit Pioneer</h1>
		<p>This is us maintaining it together - working to get 2.0 out the door.</p>
	</>
)

const notesModuleWoes = () => (
	<>
		<h1>Module System Woes // Gunnar Morling</h1>
		<p>I recently asked on Twitter and Reddit for your module system woes...</p>
		<ul>
			<li>bit.ly/jms-woes-tw</li>
			<li>bit.ly/jms-woes-rd</li>
		</ul>
		<p>... and got a good list of replies.</p>
		<p>I want to go over your replies, discuss them with my guest Gunnar (Morling; of Red Hat fame), and hopefully provide some solutions.</p>
	</>
)

const notesJavaAfterEleven = () => (
	<>
		<h1>Java After Eleven</h1>
		<p>Most projects that updated past Java 8 decided to stick to the LTS release 11. The new cadence created the illusion of not much happening after that, but nothing could be further from the truth - with new language features like switch expressions, text blocks, records, pattern matching, and sealed classes, Java is moving faster than ever.</p>
		<p>In this talk, we take a simple Java 11 code base, update it to 17, and refactor it to use the new language features and APIs. You'll be surprised how much the code changes!</p>
	</>
)

const notesLiveFx = () => (
	<>
		<h1>LiveFX</h1>
		<p>This stream's layout, i.e. the colorful appearance of windows and tabs, is a NextJS app tentatively called LiveFX.
			There's a Node server running that presents a website to an OBS browser source, which is how you get to see it.</p>
		<p>Here, I'm making that app fancier and more interactive.</p>
	</>
)

const notesLoom = () => (
	<>
		<h1>Project Loom</h1>
		<p>We're experimenting with Project Loom's virtual threads.
			You can find the sources here:
		</p>
		<p className={tabStyle.indent}>https://github.com/nipafx/loom-lab</p>
		{/* <p>If you want to join via IntelliJ's Code With Me and with mic or cam, let me know and we can make it happen.</p> */}
	</>
)

const notesJavaMap = () => (
	<>
		<h1>Java Ecosystem Map</h1>
		<p>The Java ecosystem is huge and full of jargon and technobabble.
			Many concepts are hard to understand in isolation and even harder to understand together with a bunch of other cryptic concepts.</p>
		<p>I want to change that and I think a map of the ecosystem would help here:</p>
		<ul>
			<li>concepts have simple explanations</li>
			<li>their relation are visible</li>
		</ul>
		<p>A few more details: http://bit.ly/jmap-n1</p>
	</>
)

// You. Me. #Java 18. Now.
// #Java X feature demo - today: ...
const notesJavaXDemo = () => (
	<>
		<h1>Java X Feature Demo</h1>
		<p>My feature demo is out of date and I want to spend some time moving it forward towards 18.</p>
		<p className={tabStyle.indent}>https://github.com/nipafx/demo-java-x</p>
		<p>Today I want to look into the vector API.</p>
	</>
)

const notesJava18 = () => (
	<>
		<h1>Java 18 Release (Party?)</h1>
		<p>Ask me anything about today's release of JDK 18.</p>
	</>
)

const notesJep431 = () => (
	<>
		<h1>Q&A with Stuart Marks on JEP 431</h1>
		<p>JEP 431, which is targeted for Java 21, proposes the introduction of <em>sequenced collections</em>. To catch up on it:</p>
		<ul>
			<li>read the JEP: https://openjdk.org/jeps/431</li>
			<li>watch the latest Inside Java Newscast: https://inside.java/newscast</li>
		</ul>
		<p>Today, we're discussing that proposal with the JEP owner and Java collection guru Stuart Marks.</p>
	</>
)

const notesReboot = () => (
	<>
		<h1>Rebooting the Stream</h1>
		<p>My stream layout (i.e. the boxes you see on screen right now) is a Next.js web app. (OBS shows it via an embedded browser.) I've <em>not</em> been happy with the server-side development experience and want to migrate the app to Spring Boot & React. Since most of the existing code is just frontend React, that should be pretty straightforward (famous last words, I know), so let's do it!</p>
	</>
)

export default Notes
