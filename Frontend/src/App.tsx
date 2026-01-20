import { useState } from 'react';
import type { EmailDto } from './types';
import { emailService } from './services/api/emailService';
import { Select } from './components/common/Select';
import { Button } from './components/common/Button';
import { EmailList } from './components/email/EmailList';

const EMAIL_COUNT_OPTIONS = Array.from({ length: 36 }, (_, i) => i + 15).map((num) => ({
  value: num,
  label: num.toString(),
}));

function App() {
  const [emails, setEmails] = useState<EmailDto[]>([]);
  const [emailCount, setEmailCount] = useState<number>(15);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const handleFetchEmails = async () => {
    setIsLoading(true);
    setError(null);
    
    try {
      const fetchedEmails = await emailService.fetchEmails(emailCount);
      setEmails(fetchedEmails);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to fetch emails');
      setEmails([]);
    } finally {
      setIsLoading(false);
    }
  };

  const handleRetry = () => {
    handleFetchEmails();
  };

  const handleReplySent = () => {
    // Optionally refresh emails after sending reply
    // handleFetchEmails();
  };

  return (
    <div className="min-h-screen bg-slate-50">
      {/* Header */}
      <header className="sticky top-0 z-10 bg-white border-b border-slate-200 shadow-sm">
        <div className="max-w-4xl mx-auto px-6 py-4">
          <div className="flex items-center justify-between gap-4">
            <h1 className="text-2xl font-semibold text-slate-900">
              ✉️ Email Assistant
            </h1>
            
            <div className="flex items-center gap-3">
              <div className="w-32">
                <Select
                  options={EMAIL_COUNT_OPTIONS}
                  value={emailCount}
                  onChange={(e) => setEmailCount(Number(e.target.value))}
                  className="text-sm"
                />
              </div>
              <Button
                variant="primary"
                onClick={handleFetchEmails}
                disabled={isLoading}
                isLoading={isLoading}
              >
                Fetch Emails
              </Button>
            </div>
          </div>
        </div>
      </header>

      {/* Main Content */}
      <main className="max-w-4xl mx-auto px-6 py-4">
        <EmailList
          emails={emails}
          isLoading={isLoading}
          error={error}
          onRetry={handleRetry}
          onReplySent={handleReplySent}
        />
      </main>
    </div>
  );
}

export default App;
