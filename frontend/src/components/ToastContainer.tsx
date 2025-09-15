import React from 'react';
import { createPortal } from 'react-dom';
import { AnimatePresence, motion } from 'framer-motion';
import Toast from './Toast';
import { useNotifications } from '@/contexts/NotificationContext';

const ToastContainer: React.FC = () => {
  const { notifications, removeNotification } = useNotifications();

  if (typeof window === 'undefined') {
    return null;
  }

  const container = document.getElementById('toast-container') || document.body;

  return createPortal(
    <div className="fixed top-4 right-4 z-50 space-y-2 pointer-events-none">
      <AnimatePresence>
        {notifications.map((notification) => (
          <motion.div
            key={notification.id}
            initial={{ opacity: 0, x: 300, scale: 0.8 }}
            animate={{ opacity: 1, x: 0, scale: 1 }}
            exit={{ opacity: 0, x: 300, scale: 0.8 }}
            transition={{ duration: 0.3, ease: 'easeInOut' }}
            className="pointer-events-auto"
          >
            <Toast
              {...notification}
              onRemove={removeNotification}
            />
          </motion.div>
        ))}
      </AnimatePresence>
    </div>,
    container
  );
};

export default ToastContainer;
