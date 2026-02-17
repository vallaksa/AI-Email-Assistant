# Email Assistant Frontend

A modern React 18 + TypeScript frontend for the Email Assistant application, built with Vite and Tailwind CSS.

## Features

- 📧 Fetch and display emails from Gmail inbox
- 🤖 AI-powered reply generation with iteration support
- ✏️ Manual reply editing
- 📱 Responsive design (mobile, tablet, desktop)
- 🎨 Clean, modern UI inspired by Gmail × Notion

## Tech Stack

- **React 18** - UI library
- **TypeScript** - Type safety
- **Vite** - Build tool and dev server
- **Tailwind CSS** - Utility-first CSS framework
- **Axios** - HTTP client for API calls

## Getting Started

### Prerequisites

- Node.js 20+ (or 22.12+)
- npm or yarn
- Backend API running on `http://localhost:8080` (or configure via environment variable)

### Installation

1. Install dependencies:
```bash
npm install
```

2. Create a `.env` file in the `Frontend` directory:
```bash
VITE_API_BASE_URL=http://localhost:8080
```

3. Start the development server:
```bash
npm run dev
```

The application will be available at `http://localhost:5173` (or the port Vite assigns).

### Build for Production

```bash
npm run build
```

The production build will be in the `dist` directory.

## Project Structure

```
Frontend/
├── src/
│   ├── components/
│   │   ├── common/          # Reusable UI components
│   │   │   ├── Button.tsx
│   │   │   ├── Input.tsx
│   │   │   ├── Textarea.tsx
│   │   │   ├── Select.tsx
│   │   │   ├── Card.tsx
│   │   │   ├── LoadingSpinner.tsx
│   │   │   └── ErrorMessage.tsx
│   │   ├── email/           # Email-related components
│   │   │   ├── EmailList.tsx
│   │   │   ├── EmailCard.tsx
│   │   │   ├── EmailHeader.tsx
│   │   │   └── EmailSnippet.tsx
│   │   └── reply/           # Reply-related components
│   │       ├── ReplySection.tsx
│   │       ├── InstructionInput.tsx
│   │       ├── ReplyPreview.tsx
│   │       └── ReplyActions.tsx
│   ├── services/
│   │   └── api/
│   │       └── emailService.ts
│   ├── types/
│   │   └── index.ts
│   ├── utils/
│   │   └── dateFormatter.ts
│   ├── App.tsx
│   └── main.tsx
├── tailwind.config.js
├── vite.config.ts
└── package.json
```

## Usage

### Fetching Emails

1. Select the number of emails to fetch (15-50) from the dropdown
2. Click "Fetch Emails" button
3. Emails will be displayed in a scrollable list

### Replying to Emails

1. Click "Reply →" on any email card
2. (Optional) Enter a custom instruction for the AI
3. Click "Generate Reply" to create an AI-powered reply
4. Review the generated reply
5. Optionally:
   - Click "Regenerate" to create a new reply with the same instruction
   - Click "Edit" to manually edit the reply
   - Modify the instruction and click "Regenerate" for a different style
6. Click "Send Reply" when satisfied

## API Integration

The frontend communicates with the backend API at `/api/emails`:

- `POST /api/emails/fetch/{limit}` - Fetch emails
- `POST /api/emails/reply` - Generate and send reply

See the backend README for API documentation.

## Styling

The application uses Tailwind CSS with a custom color palette:

- **Primary**: Indigo (`indigo-600`)
- **Secondary**: Slate (`slate-600`)
- **Success**: Emerald (`emerald-500`)
- **Error**: Rose (`rose-500`)

Custom styles are defined in `tailwind.config.js` and `src/index.css`.

## Development

### Available Scripts

- `npm run dev` - Start development server
- `npm run build` - Build for production
- `npm run preview` - Preview production build
- `npm run lint` - Run ESLint

### Environment Variables

- `VITE_API_BASE_URL` - Backend API base URL (default: `http://localhost:8080`)

## Browser Support

- Chrome (latest)
- Firefox (latest)
- Safari (latest)
- Edge (latest)

## License

Same as the main Email Assistant project.
