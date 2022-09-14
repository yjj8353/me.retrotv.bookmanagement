/**
 * 새로운 Member 객체를 생성함.
 * @class
 * @implements {IMember}
 */
export class Member implements IMember {
  id?: number | null
  username?: string | null
  password?: string | null
  passwordCheck?: string | null
  realName?: string | null
  email?: string | null
  refreshToken?: string | null
  role?: string | null

  /**
   * @constructor
   * @param memberBuilder Member Builder 객체
   */
  constructor(
    memberBuilder: MemberBuilder
  ) {
    this.id = memberBuilder.__id;
    this.username = memberBuilder.__username;
    this.password = memberBuilder.__password;
    this.passwordCheck = memberBuilder.__passwordCheck
    this.realName = memberBuilder.__realName;
    this.email = memberBuilder.__email;
    this.refreshToken = memberBuilder.__refreshToken;
    this.role = memberBuilder.__role;
  }
}

/**
 * @interface
 */
export interface IMember {
  id?: number | null
  username?: string | null
  password?: string | null
  passwordCheck?: string | null
  realName?: string | null
  email?: string | null
  refreshToken?: string | null
  role?: string | null
}

/**
 * Member에 대한 Builder 클래스.
 * @builder
 */
export class MemberBuilder {
  __id?: number | null;
  __username?: string | null;
  __password?: string | null;
  __passwordCheck?: string | null;
  __realName?: string | null;
  __email?: string | null;
  __refreshToken?: string | null;
  __role?: string | null;

  constructor() {
    return this;
  }

  id(id: number | null) {
    this.__id = id;
    return this;
  }

  username(username: string | null) {
    this.__username = username;
    return this;
  }

  password(password: string | null) {
    this.__password = password;
    return this;
  }

  passwordCheck(passwordCheck: string | null) {
    this.__passwordCheck = passwordCheck;
    return this;
  }

  realName(realName: string | null) {
    this.__realName = realName;
    return this;
  }

  email(email: string | null) {
    this.__email = email;
    return this;
  }

  refreshToken(refreshToken: string | null) {
    this.__refreshToken = refreshToken;
    return this;
  }

  role(role: string | null) {
    this.__role = role;
    return this;
  }

  build() {
    return new Member(this);
  }
}
