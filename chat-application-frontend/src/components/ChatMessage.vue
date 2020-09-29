<template>
  <div>
    <div class="message-data">
      <span class="message-data-name"> {{ user }}</span>
      <span class="message-data-time">{{
        isHistory
          ? "History: " + new Date(timestamp).toLocaleTimeString("en-US")
          : new Date(timestamp).toLocaleTimeString("en-US")
      }}</span>
    </div>
    <div class="message" :class="classObject">
      {{ text }}
    </div>
  </div>
</template>

<script>
export default {
  name: 'ChatMessage',
  props: ['user', 'text', 'timestamp', 'isHistory'],
  computed: {
    classObject() {
      return {
        'message-history': this.isHistory,
        'message-self':
          !this.isHistory && this.user === this.$store.state.currentUser,
        'message-other':
          !this.isHistory && this.user !== this.$store.state.currentUser,
      };
    },
  },
};
</script>

<style lang="scss">
@import "../assets/main.scss";

.message-data {
  margin-bottom: 12px;
  font-size: 0.8em;
}

.message-data-name {
  margin-right: 15px;
}

.message-data-time {
  color: lighten($gray, 8%);
  padding-left: 6px;
}

.message {
  display: inline-flex;
  padding: 5px 10px;
  line-height: 26px;
  font-size: 16px;
  border-radius: 7px;
  margin-bottom: 15px;
  position: relative;
}

.message::after {
  bottom: 100%;
  left: 20px;
  border: solid transparent;
  content: " ";
  height: 0;
  width: 0;
  position: absolute;
  pointer-events: none;
  border-width: 10px;
  margin-left: -10px;
}

.message-self {
  background: $one;
}

.message-self::after {
  border-bottom-color: $one;
}

.message-other {
  background: $two;
}

.message-other::after {
  border-bottom-color: $two;
}

.message-history {
  background: $three;
}

.message-history::after {
  border-bottom-color: $three;
}
</style>
