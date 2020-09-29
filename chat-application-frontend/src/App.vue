<template>
  <ChatApplication />
</template>

<script>
import ChatApplication from './components/ChatApplication.vue';

export default {
  name: 'App',
  components: {
    ChatApplication,
  },
};
</script>

<style>
#app {
  height: 100vh;
  min-height: 100vh;
  display: block;
  margin-left: auto;
  margin-right: auto;
  width: 90%;
}

::-webkit-scrollbar {
  width: 5px;
}

::-webkit-scrollbar-track {
  background: #f1f1f1;
}

::-webkit-scrollbar-thumb {
  background: #888;
}

::-webkit-scrollbar-thumb:hover {
  background: #555;
}
</style>
