<template>
  <n-config-provider :theme-overrides="themeOverrides">
    <n-message-provider>
      <n-dialog-provider>
        <n-notification-provider>
          <router-view />
        </n-notification-provider>
      </n-dialog-provider>
    </n-message-provider>
  </n-config-provider>
</template>

<script setup>
import { NConfigProvider, NMessageProvider, NDialogProvider, NNotificationProvider } from 'naive-ui'

const themeOverrides = {
  common: {
    primaryColor: '#18a058',
    primaryColorHover: '#36ad6a',
    primaryColorPressed: '#0c7a43'
  }
}
</script>

<style>
* {
  box-sizing: border-box;
}

body {
  margin: 0;
  padding: 0;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}

html, body, #app {
  height: 100%;
  width: 100%;
  overflow-x: hidden;
}

.page-container {
  padding: 16px;
}

@media (max-width: 768px) {
  .page-container {
    padding: 12px;
  }
  
  .n-card {
    --n-padding-bottom: 12px !important;
    --n-padding-left: 12px !important;
    --n-padding-right: 12px !important;
    --n-padding-top: 12px !important;
  }
  
  .n-data-table {
    font-size: 13px;
  }
  
  .n-data-table .n-data-table-th,
  .n-data-table .n-data-table-td {
    padding: 8px 6px;
  }
  
  .n-form-item {
    --n-label-height: 24px !important;
  }
  
  .n-button {
    --n-height: 32px !important;
    --n-padding: 0 12px !important;
  }
  
  .n-input {
    --n-height: 32px !important;
  }
  
  .n-select {
    --n-height: 32px !important;
  }
}

@media (max-width: 480px) {
  .page-container {
    padding: 8px;
  }
  
  .n-card {
    --n-padding-bottom: 8px !important;
    --n-padding-left: 8px !important;
    --n-padding-right: 8px !important;
    --n-padding-top: 8px !important;
  }
  
  .n-data-table-wrapper {
    overflow-x: auto;
  }
  
  .n-space {
    --n-gap: 8px 8px !important;
  }
}

::-webkit-scrollbar {
  width: 6px;
  height: 6px;
}

::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 3px;
}

::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 3px;
}

::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}
</style>
