/**
 * Format ISO 8601 date string to readable format
 */
export const formatDate = (dateString: string): string => {
  const date = new Date(dateString);
  const now = new Date();
  const diffInMs = now.getTime() - date.getTime();
  const diffInDays = Math.floor(diffInMs / (1000 * 60 * 60 * 24));

  // Format options
  const sameDayOptions: Intl.DateTimeFormatOptions = {
    hour: 'numeric',
    minute: '2-digit',
  };

  const sameYearOptions: Intl.DateTimeFormatOptions = {
    month: 'short',
    day: 'numeric',
    hour: 'numeric',
    minute: '2-digit',
  };

  const fullOptions: Intl.DateTimeFormatOptions = {
    year: 'numeric',
    month: 'short',
    day: 'numeric',
    hour: 'numeric',
    minute: '2-digit',
  };

  if (diffInDays === 0) {
    // Today: show time only
    return `Today, ${date.toLocaleTimeString('en-US', sameDayOptions)}`;
  } else if (diffInDays === 1) {
    // Yesterday
    return `Yesterday, ${date.toLocaleTimeString('en-US', sameDayOptions)}`;
  } else if (date.getFullYear() === now.getFullYear()) {
    // This year: show month, day, and time
    return date.toLocaleDateString('en-US', sameYearOptions);
  } else {
    // Different year: show full date
    return date.toLocaleDateString('en-US', fullOptions);
  }
};
