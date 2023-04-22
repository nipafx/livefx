import {useState, useEffect} from 'react';

const CLIENT_ID = process.env.REACT_APP_TWITCH_CLIENT_ID;
const REDIRECT_URI = "http://localhost:3000";

export class TwitchEmote {
    constructor({id, code, emote_type}) {
        this.id = id;
        this.code = code;
        this.type = emote_type;
    }

    get url() {
        const theme = "dark";
        const format = "default";
        return TwitchEmote.url_template
            .replace("{{format}}", format)
            .replace("{{theme}}", theme)
            .replace("{{id}}", this.id)
            .replace("{{scale}}", TwitchEmote.scale_template.x1);
    }

    getUrlForScale(scale) {
        const theme = "dark";
        const format = "default";
        return TwitchEmote.url_template
            .replace("{{format}}", format)
            .replace("{{theme}}", theme)
            .replace("{{id}}", this.id)
            .replace("{{scale}}", TwitchEmote.scale_template[scale]);
    }

    get x1Url() {
        return this.getUrlForScale("x1");
    }

    get x2Url() {
        return this.getUrlForScale("x2");
    }

    get x3Url() {
        return this.getUrlForScale("x3");
    }

    static get url_template() {
        return "https://static-cdn.jtvnw.net/emoticons/v2/{{id}}/{{format}}/{{theme}}/{{scale}}";
    }

    static get scale_template() {
        return {
            x1: "1.0",
            x2: "2.0",
            x3: "3.0",
        };
    }
}

export default function TwitchEmotes() {
    const [globalEmotes, setGlobalEmotes] = useState({});
    const token = process.env.REACT_APP_TWITCH_USER_TOKEN;
    const url = `https://api.twitch.tv/helix/chat/emotes/global?client_id=` + CLIENT_ID
    useEffect(() => {
        async function fetchGlobalEmotes() {
            const res = await fetch(url, {
                headers: {
                    'Authorization': 'Bearer ' + token,
                    'Client-Id': CLIENT_ID
                }
            });
            const data = await res.json()
            for (let emote of data.data) {
                globalEmotes[emote.name] = new TwitchEmote(emote);
            }
            setGlobalEmotes(globalEmotes);
        }

        fetchGlobalEmotes();
    }, []);

    return (
        globalEmotes
    );
}