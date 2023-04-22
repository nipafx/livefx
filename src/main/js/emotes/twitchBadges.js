import {useState, useEffect} from 'react';

const CLIENT_ID = process.env.REACT_APP_TWITCH_CLIENT_ID;
const BROADCASTER_ID = process.env.REACT_APP_TWITCH_USER_ID

export class Badge {
    constructor({versions}) {
        this.versions = versions;
    }

    get url() {
        const theme = "dark";
        const format = "default";
        return Badge.url_template
            .replace("{{format}}", format)
            .replace("{{theme}}", theme)
            .replace("{{scale}}", Badge.scale_template.x1);
    }

    getUrlForScale(scale) {
        const theme = "dark";
        const format = "default";
        return Badge.url_template
            .replace("{{format}}", format)
            .replace("{{theme}}", theme)
            .replace("{{scale}}", Badge.scale_template[scale]);
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

export default function TwitchBadges() {
    const [globalBadges, setGlobalBadges] = useState({});
    const token = process.env.REACT_APP_TWITCH_USER_TOKEN;
    const url = `https://badges.twitch.tv/v1/badges/global/display?client_id=` + CLIENT_ID
    const urlChannel = `https://badges.twitch.tv/v1/badges/channels/` + BROADCASTER_ID + `/display`
    useEffect(() => {
        async function fetchGlobalBadges() {
            const res = await fetch(url, {
                headers: {
                    'Authorization': 'Bearer ' + token,
                    'Client-Id': CLIENT_ID
                }
            });
            const data = await res.json()
            for (const badge in data.badge_sets) {
                globalBadges[badge] = new Badge(data.badge_sets[badge])
            }
            setGlobalBadges(globalBadges);
        }

        async function fetchChannelBadges() {
            const res = await fetch(urlChannel, {
                headers: {
                    'Authorization': 'Bearer ' + token,
                    'Client-Id': CLIENT_ID
                }
            });
            const data = await res.json()
            for (const badge in data.badge_sets) {
                globalBadges[badge] = new Badge(data.badge_sets[badge])
            }
            setGlobalBadges(globalBadges);
        }

        fetchGlobalBadges();
        fetchChannelBadges();
    }, []);

    return (
        globalBadges
    );
}