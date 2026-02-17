import { useState } from 'react';
import type { EmailDto } from './types';
import { emailService } from './services/api/emailService';
import { Select } from './components/common/Select';
import { Button } from './components/common/Button';
import { Tabs } from './components/common/Tabs';
import { EmailList } from './components/email/EmailList';
import { EmailDetailPanel } from './components/email/EmailDetailPanel';

const EMAIL_COUNT_OPTIONS = Array.from({ length: 9 }, (_, i) => 10 + (i * 5)).map((num) => ({
  value: num,
  label: num.toString(),
}));

type TabType = 'inbox' | 'sent';

const TABS = [
  { id: 'inbox', label: 'Inbox' },
  { id: 'sent', label: 'Sent' },
];

function App() {
  const [activeTab, setActiveTab] = useState<TabType>('inbox');
  // Separate caches for inbox and sent emails
  const [inboxEmails, setInboxEmails] = useState<EmailDto[]>([]);
  const [sentEmails, setSentEmails] = useState<EmailDto[]>([]);
  const [emailCount, setEmailCount] = useState<number>(10);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [selectedEmail, setSelectedEmail] = useState<EmailDto | null>(null);
  const [isDetailPanelOpen, setIsDetailPanelOpen] = useState(false);

  // Get emails for current tab
  const emails = activeTab === 'inbox' ? inboxEmails : sentEmails;

  const handleFetchEmails = async () => {
    setIsLoading(true);
    setError(null);
    
    try {
      const fetchedEmails = activeTab === 'inbox'
        ? await emailService.fetchEmails(emailCount)
        : await emailService.fetchSentEmails(emailCount);
      
      // Update the appropriate cache based on active tab
      if (activeTab === 'inbox') {
        setInboxEmails(fetchedEmails);
      } else {
        setSentEmails(fetchedEmails);
      }
    } catch (err) {
      setError(err instanceof Error ? err.message : `Failed to fetch ${activeTab} emails`);
      // Clear the cache for the current tab on error
      if (activeTab === 'inbox') {
        setInboxEmails([]);
      } else {
        setSentEmails([]);
      }
    } finally {
      setIsLoading(false);
    }
  };

  const handleTabChange = (tabId: string) => {
    setActiveTab(tabId as TabType);
    setError(null);
    // Don't clear emails - keep them cached for the session
  };

  const handleRetry = () => {
    handleFetchEmails();
  };

  const handleReplySent = () => {
    // Optionally refresh emails after sending reply to show new messages
    // Uncomment to auto-refresh:
    // handleFetchEmails();
  };

  const handleEmailClick = (email: EmailDto) => {
    setSelectedEmail(email);
    setIsDetailPanelOpen(true);
  };

  const handleCloseDetailPanel = () => {
    setIsDetailPanelOpen(false);
    setSelectedEmail(null);
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
                Fetch {activeTab === 'inbox' ? 'Emails' : 'Sent'}
              </Button>
            </div>
          </div>
        </div>
      </header>

      {/* Main Content */}
      <main className="max-w-4xl mx-auto px-6 py-4">
        <div className="bg-white rounded-lg shadow-sm mb-4">
          <Tabs
            tabs={TABS}
            activeTab={activeTab}
            onTabChange={handleTabChange}
          />
        </div>
        <EmailList
          emails={emails}
          isLoading={isLoading}
          error={error}
          onRetry={handleRetry}
          onReplySent={handleReplySent}
          onEmailClick={handleEmailClick}
        />
      </main>

      {/* Email Detail Panel */}
      <EmailDetailPanel
        email={selectedEmail}
        isOpen={isDetailPanelOpen}
        onClose={handleCloseDetailPanel}
        onReplySent={handleReplySent}
      />
    </div>
  );
}

export default App;
