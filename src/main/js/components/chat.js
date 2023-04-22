import style from './chat.module.css'
import tabStyle from './tab.module.css'
import React from 'react'
import BTTVEmotes from "../emotes/bttvEmotes";
import TwitchEmotes from "../emotes/twitchEmotes";
import SevenTVEmotes from "../emotes/SevenTVEmotes";
import TwitchBadges from "../emotes/twitchBadges";


const Chat = ({messages}) => {
    const bttvGlobalEmotes = BTTVEmotes()
    const twitchGlobalEmotes = TwitchEmotes()
    const sevenTVGlobalEmotes = SevenTVEmotes()
    const twitchGlobalBadges = TwitchBadges()
    const globalEmotes = [
        {
            service: "bttv",
            serviceGlobalEmotes: bttvGlobalEmotes
        },
        {
            service: "twitch",
            serviceGlobalEmotes: twitchGlobalEmotes
        },
        {
            service: "sevenTV",
            serviceGlobalEmotes: sevenTVGlobalEmotes
        }]
    const reorderedMessages = [...messages]
    reorderedMessages.reverse()
    return (
        <div className={tabStyle.content + " " + style.container}>
            <div className={style.messageBox}>{messages.map(message => {
                let msg_tags = parseTags(message.tags)
                return <Message key={msg_tags.id} message={message}
                                globalEmotes={globalEmotes} globalBadges={twitchGlobalBadges}/>
            })
            }
            </div>
        </div>
    )
}

const Message = ({message, globalEmotes, globalBadges}) => {
    const wordEmoteMap = parseEmotes(message.text, globalEmotes)
    const msg_tags = parseTags(message.tags)
    const badgesRaw = msg_tags.badges.split(',')
    const badges = badgesRaw.map(badgeRaw => {
        return badgeRaw.split('/')
    })
    const showBadge = badges === ""
    const msg_words = message.text.split(" ");
    // actually replace everything collected at once
    let parsed_msg = msg_words.map(word => {
        // replace emotes bttv
        if (wordEmoteMap[word]) {
            return Emote(wordEmoteMap[word]);
        }
        return word + " ";
    });
    return (<div className={style.message}>
        <span className={showBadge ? 'hidden' : style.badge}>{badges.map(badge => {
                if (badges !== "") {
                    let badge_url;
                    if (badge[0] === "subscriber") {
                        badge_url = getSubBadge(badge[1]) || getBadgeByNameAndVersion(badge[0], badge[1], globalBadges);
                        return <img className={style.badge} alt={badge[0]} src={badge_url} width="18" height="18"/>
                    }
                    badge_url = getBadgeByNameAndVersion(badge[0], badge[1], globalBadges);
                    return <img className={style.badge} alt={badge[0]} src={badge_url} width="18" height="18"/>
                }
            }
        )}
        </span>
        <span className={style.nick}>{message.nick}</span>
        <span className={style.text}>{parsed_msg}</span>
    </div>)
}

const getSubBadge = (version, twitchGlobalBadges) => {
    if (twitchGlobalBadges["subscriber"]) {
        return twitchGlobalBadges["subscriber"].versions[version].image_url_2x;
    }
};

const getBadgeByNameAndVersion = (name, version, twitchGlobalBadges) => {
    const badge = twitchGlobalBadges[name];
    if (badge) {
        if (badge.versions[version]) {
            return badge.versions[version].image_url_2x;
        } else {
            return badge.versions[Object.keys(badge.versions)[0]].image_url_2x;
        }
    }
}

const parseTags = (tagsString) => {
    const tagMap = {};
    const rawTags = tagsString.split(';')
    rawTags.forEach(str => {
        let tag = str.split('=')
        if (tagMap[0] !== "")
            tagMap[tag[0]] = tag[1]
    })
    return tagMap
}
const parseEmotes = (message_text, globalEmotes) => {
    const wordEmoteMap = {};
    const msg_words = message_text.split(" ");
    // find words to replace with
    msg_words.forEach(str => {
        // global emotes repalcement
        globalEmotes.forEach(service => {
            for (const key in service.serviceGlobalEmotes) {
                if (str === key) {
                    wordEmoteMap[key] = {
                        name: key,
                        service: service.service,
                        emote: service.serviceGlobalEmotes[key],
                    };
                }
            }
        });
    });
    return wordEmoteMap;

}

const Emote = (wordEmote) => {
    return (
        <img name={wordEmote.name} src={wordEmote.emote.url} alt={wordEmote.name}/>
    );
}

export default Chat
