import { Notify } from 'quasar';

export const NotifyMixin = {
  name: 'NotifyMixin',

  methods: {

    /**
     * 사용자에게 메시지를 보여주기 위한 Quasar Notify 기능을 간략화한 함수.
     * @param message Quasar Notify로 표시할 메시지
     * @param type Quasar Notify의 타입 (NotifyType enum 참조)
     * @param position Quasar Notify를 표시할 위치 (NotifyPosition enum 참조)
     */
    notify(
      message: string,
      type: NotifyType,
      position: NotifyPosition = NotifyPosition.BOTTOM
    ) {
      Notify.create({
        message: message,
        type: type,
        position: position,
      });
    },
  },
};

/**
 * Quasar Notify에 사용하는 type
 * @readonly
 * @enum
 */
export enum NotifyType {
  POSITIVE = 'positive',
  NEGATIVE = 'negative',
  WARNING = 'warning',
  INFO = 'info',
  ONGOING = 'ongoing',
}

/**
 * Quasar Notify에 사용하는 position
 * @readonly
 * @enum
 */
export enum NotifyPosition {
  TOP_LEFT = 'top-left',
  TOP_RIGHT = 'top-right',
  BOTTOM_LEFT = 'bottom-left',
  BOTTOM_RIGHT = 'bottom-right',
  TOP = 'top',
  BOTTOM = 'bottom',
  LEFT = 'left',
  RIGHT = 'right',
  CENTER = 'center',
}
