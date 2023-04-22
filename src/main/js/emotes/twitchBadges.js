import {useState, useEffect} from 'react';

const CLIENT_ID = process.env.REACT_APP_TWITCH_CLIENT_ID;
const BROADCASTER_ID = process.env.REACT_APP_TWITCH_USER_ID

export class Badge {
    constructor({versions}) {
        this.versions = versions;
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