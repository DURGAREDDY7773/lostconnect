# LostConnect Commercial App Extension Report

## LLM Declaration

ChatGPT/Codex was used to support this assessment by providing code guidance, debugging help, implementation planning, and report drafting. The generated suggestions were reviewed and adapted for the LostConnect Android project requirements, including Java, SQLite, Google Maps, Places autocomplete, runtime permissions, and radius-based search.

## Research Report

### Why Commercial Version?

A commercial version of LostConnect would be valuable because lost item management is a real-world problem for universities, shopping centres, public transport providers, workplaces, and community venues. In the current simple version, users can post lost or found items and view them locally, but a commercial app could make recovery faster by using live location services, cloud storage, and automated matching. Faster recovery improves user experience because people can search nearby instead of manually checking long lists. It also supports better safety because users can share only moderated contact details and meet through approved pickup points. Community support is another major benefit: people who find items can quickly report them, while owners can receive alerts when a similar item appears nearby.

A commercial LostConnect app would use Firebase as the backend, with a cloud database for shared lost/found posts instead of only local SQLite storage. Users would sign in with a secure login system so posts can be linked to verified accounts. Push notifications would alert users when an item matching their description, category, image, or location is added. Live GPS tracking could help users search around the last known location of an item, while admin moderation would reduce spam, unsafe messages, and false reports. Cloud Storage would store uploaded item images, and AI-based item matching could compare names, descriptions, categories, images, dates, and locations to recommend likely matches. This commercial version would therefore move LostConnect from a single-device app into a scalable, safer, community-driven recovery platform.

### Architecture Diagram

```text
Android App
     ↓
Google Maps API / Places API
     ↓
Firebase / SQLite
     ↓
Cloud Storage
     ↓
Notification Service
```

## Submission Links

YouTube demo link: TODO - add your app demo video URL

GitHub repository link: TODO - add your public GitHub repository URL

LLM conversation link: TODO - add your ChatGPT conversation URL

## Testing Checklist

- Add item
- Save to SQLite
- Current location works
- Google map loads
- Markers visible
- Radius filter works
- Autocomplete works
- Image upload works
- App does not crash
