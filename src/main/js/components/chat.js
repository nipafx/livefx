import style from './chat.module.css'
import tabStyle from './tab.module.css'

const Chat = ({ messages }) => {
	return (
		<div className={tabStyle.content + " " + style.container}>
			<div className={style.messageBox}>
				{messages.map(message => <Message key={message.id} message={message} />)}
			</div>
		</div>
	)
}

const Message = ({ message }) => (
	<div className={style.message}>
		<div className={style.nick}>{message.nick}</div>
		<div className={style.text}>
			{message.blocks
				.map((block, index) => {
					switch (block.type) {
						case "paragraph":
							return <p key={index} dangerouslySetInnerHTML={{ __html: block.text }} />
						case "code":
							return <pre key={index}><code>{block.text}</code></pre>
						default:
							return null
					}
				})
			}
		</div>
	</div>
)

export default Chat
