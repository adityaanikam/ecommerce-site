import React, { useEffect, useState, useCallback } from 'react';
import { Modal, Button } from '@/components/ui';
import { Clock, AlertTriangle } from 'lucide-react';
import { useAuth } from '@/contexts/AuthContext';
import { useNotifications } from '@/contexts/NotificationContext';
import TokenManager from '@/lib/tokenManager';

interface SessionTimeoutProps {
  warningTime?: number; // Time in milliseconds before showing warning
  timeoutTime?: number; // Time in milliseconds before auto-logout
  onTimeout?: () => void;
}

export const SessionTimeout: React.FC<SessionTimeoutProps> = ({
  warningTime = 2 * 60 * 1000, // 2 minutes
  timeoutTime = 15 * 60 * 1000, // 15 minutes
  onTimeout,
}) => {
  const { logout } = useAuth();
  const { showWarning } = useNotifications();
  const [showWarningModal, setShowWarningModal] = useState(false);
  const [timeLeft, setTimeLeft] = useState(0);
  const [isExtending, setIsExtending] = useState(false);

  // Format time for display
  const formatTime = useCallback((milliseconds: number): string => {
    const minutes = Math.floor(milliseconds / 60000);
    const seconds = Math.floor((milliseconds % 60000) / 1000);
    return `${minutes}:${seconds.toString().padStart(2, '0')}`;
  }, []);

  // Extend session
  const extendSession = useCallback(async () => {
    setIsExtending(true);
    try {
      // Update session timeout
      TokenManager.updateSessionTimeout();
      
      // Show success message
      showWarning('Session extended successfully');
      
      // Close warning modal
      setShowWarningModal(false);
    } catch (error) {
      console.error('Failed to extend session:', error instanceof Error ? error.message : String(error));
      showWarning('Failed to extend session. Please log in again.');
    } finally {
      setIsExtending(false);
    }
  }, [showWarning]);

  // Logout user
  const handleLogout = useCallback(async () => {
    try {
      await logout();
      onTimeout?.();
    } catch (error) {
      console.error('Logout failed:', error instanceof Error ? error.message : String(error));
    }
  }, [logout, onTimeout]);

  // Setup session timeout monitoring
  useEffect(() => {
    let intervalId: NodeJS.Timeout;
    let warningShown = false;

    const checkSession = () => {
      if (!TokenManager.isAuthenticated()) {
        return;
      }

      const timeUntilExpiry = TokenManager.getTimeUntilExpiry();
      
      // If session is expired, logout immediately
      if (timeUntilExpiry <= 0) {
        handleLogout();
        return;
      }

      // Show warning if time is running out
      if (timeUntilExpiry <= warningTime && !warningShown) {
        warningShown = true;
        setTimeLeft(timeUntilExpiry);
        setShowWarningModal(true);
      }

      // Update time left in warning modal
      if (showWarningModal && timeUntilExpiry > 0) {
        setTimeLeft(timeUntilExpiry);
      }
    };

    // Check session every second
    intervalId = setInterval(checkSession, 1000);

    // Initial check
    checkSession();

    return () => {
      if (intervalId) {
        clearInterval(intervalId);
      }
    };
  }, [warningTime, handleLogout, showWarningModal]);

  // Auto-logout when time runs out
  useEffect(() => {
    if (showWarningModal && timeLeft <= 0) {
      setShowWarningModal(false);
      handleLogout();
    }
  }, [timeLeft, showWarningModal, handleLogout]);

  // Handle user activity to extend session
  useEffect(() => {
    const handleUserActivity = () => {
      if (TokenManager.isAuthenticated()) {
        TokenManager.updateSessionTimeout();
        
        // Hide warning modal if it's showing
        if (showWarningModal) {
          setShowWarningModal(false);
        }
      }
    };

    // List of events that indicate user activity
    const events = [
      'mousedown',
      'mousemove',
      'keypress',
      'scroll',
      'touchstart',
      'click',
    ];

    // Add event listeners
    events.forEach(event => {
      document.addEventListener(event, handleUserActivity, true);
    });

    return () => {
      // Remove event listeners
      events.forEach(event => {
        document.removeEventListener(event, handleUserActivity, true);
      });
    };
  }, [showWarningModal]);

  return (
    <>
      {/* Session Warning Modal */}
      <Modal
        isOpen={showWarningModal}
        onClose={() => {}} // Prevent closing by clicking outside
        title="Session Timeout Warning"
        size="sm"
      >
        <div className="text-center p-6">
          <div className="w-16 h-16 bg-warning-100 dark:bg-warning-900 rounded-full flex items-center justify-center mx-auto mb-4">
            <Clock className="h-8 w-8 text-warning-600 dark:text-warning-400" />
          </div>
          
          <h3 className="text-lg font-semibold text-secondary-900 dark:text-secondary-100 mb-2">
            Your session will expire soon
          </h3>
          
          <p className="text-secondary-600 dark:text-secondary-400 mb-4">
            You will be automatically logged out in:
          </p>
          
          <div className="text-2xl font-mono font-bold text-warning-600 dark:text-warning-400 mb-6">
            {formatTime(timeLeft)}
          </div>
          
          <div className="flex gap-3 justify-center">
            <Button
              onClick={extendSession}
              disabled={isExtending}
              className="flex-1"
            >
              {isExtending ? 'Extending...' : 'Extend Session'}
            </Button>
            
            <Button
              onClick={handleLogout}
              variant="outline"
              className="flex-1"
            >
              Logout Now
            </Button>
          </div>
          
          <p className="text-xs text-secondary-500 dark:text-secondary-400 mt-4">
            Click anywhere or move your mouse to extend the session
          </p>
        </div>
      </Modal>
    </>
  );
};

// Hook for session timeout management
export const useSessionTimeout = () => {
  const [timeUntilExpiry, setTimeUntilExpiry] = useState(0);
  const [isWarning, setIsWarning] = useState(false);

  useEffect(() => {
    const updateTime = () => {
      const timeLeft = TokenManager.getTimeUntilExpiry();
      setTimeUntilExpiry(timeLeft);
      setIsWarning(timeLeft <= 2 * 60 * 1000); // Warning at 2 minutes
    };

    // Update every second
    const interval = setInterval(updateTime, 1000);
    updateTime(); // Initial update

    return () => clearInterval(interval);
  }, []);

  const extendSession = () => {
    TokenManager.updateSessionTimeout();
  };

  const formatTime = (milliseconds: number): string => {
    const minutes = Math.floor(milliseconds / 60000);
    const seconds = Math.floor((milliseconds % 60000) / 1000);
    return `${minutes}:${seconds.toString().padStart(2, '0')}`;
  };

  return {
    timeUntilExpiry,
    isWarning,
    extendSession,
    formatTime: formatTime(timeUntilExpiry),
  };
};

// Session status indicator component
export const SessionStatus: React.FC = () => {
  const { timeUntilExpiry, isWarning, formatTime } = useSessionTimeout();
  const { isAuthenticated } = useAuth();

  if (!isAuthenticated || timeUntilExpiry <= 0) {
    return null;
  }

  return (
    <div className={`flex items-center gap-2 text-xs px-2 py-1 rounded ${
      isWarning 
        ? 'bg-warning-100 text-warning-700 dark:bg-warning-900 dark:text-warning-300'
        : 'bg-success-100 text-success-700 dark:bg-success-900 dark:text-success-300'
    }`}>
      <Clock className="h-3 w-3" />
      <span>Session: {formatTime}</span>
    </div>
  );
};

export default SessionTimeout;
