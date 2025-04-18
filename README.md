# CatchFood API 서버

## ERD
> 준비중

## API 명세

### 공통 Request Header

| 헤더 이름   | 설명                                                                                      |
| ----------- | ----------------------------------------------------------------------------------------- |
| X-User-Name | 현재 로그인 한 유저의 이름입니다. 이 값을 보내면 해당 유저로 로그인 한 것으로 판단합니다. |

### GET /parties

> 내 같이 먹을 그룹 목록

#### Response Body

```json
{
  "status": "SUCCESS",
  "data": [
    {
      "id": 1,
      "name": "파티 이름",
      "members": ["김진홍", "정종찬", "방유빈"]
    }
  ],
  "message": null
}
```

### POST /parties

> 같이 먹을 그룹 만들기

#### Request Body

```json
{
  "partyName": "파티 이름"
}
```

#### Response Body

```json
{
  "status": "SUCCESS",
  "data": null,
  "message": null
}
```

### GET /parties/{id}/members

> 그룹의 멤버 조회

#### Response Body

```json
{
  "status": "SUCCESS",
  "data": ["김진홍", "정종찬", "방유빈"],
  "message": null
}
```

### POST /parties/{id}/members

> 그룹에 멤버 초대

#### Request Body

```json
{
  "memberName": "김진홍"
}
```

#### Response Body

```json
{
  "status": "SUCCESS",
  "data": null,
  "message": null
}
```

### POST /parties/{id}/polls

> '음식 추천받기' 플로우 시작

#### Response Body

```json
{
  "status": "SUCCESS",
  "data": null,
  "message": null
}
```

### GET /parties/{id}/polls

> 그룹에 개설된 설문들 목록

#### Response Body

```json
{
  "status": "SUCCESS",
  "data": [
    {
      "done": [1, 2],
      "ongoing": 3
    }
  ],
  "message": null
}
```

### GET /parties/{id}/polls/{id}

> 설문 상세조회

#### Response Body

```json
{
  "status": "SUCCESS",
  "data": {
    "status": "IN_PROGRESS", // IN_PROGRESS, DONE
    "preferences": {
      "김진홍": "매운 거",
      "정종찬": "한식 아무거나"
    },
    "recommendedStores": [
      {
        "id": 1,
        "storeName": "홍콩반점",
        "representativeMenu": {
          "name": "짬뽕",
          "imageUrl": ""
        },
        "category": "중식",
        "distanceInMinutesByWalk": 10,
        "votedMembers": ["김진홍"],
        "isVotedByMe": true
      }
    ]
  },
  "message": null
}
```

### POST /parties/{id}/polls/{id}/preferences

> 오늘 먹고싶은 것에 대해 메시지 입력

#### Request Body

```json
{
  "preference": "매운 거"
}
```

### Response Body

```json
{
  "status": "SUCCESS",
  "data": null,
  "message": null
}
```

### POST /parties/{id}/polls/{id}/recommended-stores/{id}/vote

> 투표

#### Response Body

```json
{
  "status": "SUCCESS",
  "data": null,
  "message": null
}
```

### GET /members

> 전체 회원 조회 (초대 가능한 멤버 조회할때 사용)

#### Response Body

```json
{
  "status": "SUCCESS",
  "data": [
    {
      "name": "김진홍"
    }
  ],
  "message": null
}
```

### GET /stores

> 식당 검색

#### Request Parameters

| 헤더 이름 | 설명                                 |
| --------- | ------------------------------------ |
| keyword   | 검색 키워드 (입력 안할 시 전체 목록) |

#### Response Body

```json
{
  "status": "SUCCESS",
  "data": [
    {
      "id": 1,
      "storeName": "홍콩반점",
      "category": "중식",
      "representativeMenu": {
        "name": "짬뽕",
        "imageUrl": ""
      },
      "distanceInMinutesByWalk": 10
    }
  ],
  "message": null
}
```

### GET /stores/{id}

> 식당 상세조회

#### Response Body

```json
{
  "status": "SUCCESS",
  "data": {
    "id": 1,
    "storeName": "홍콩반점",
    "category": "중식",
    "distanceInMinutesByWalk": 10,
    "contact": "02-1234-4567",
    "address": "서울특별시 중구 대변대로 12-1",
    "businessHours": {
      "open": "09:00",
      "close": "18:00"
    },
    "menus": [
      {
        "menuName": "짬뽕",
        "imageUrl": "",
        "price": 10000
      }
    ],
    "ratingStars": 4.5
  },
  "message": null
}
```

### GET /my/preferences

> 내 취향 조회

#### Response Body

```json
{
  "status": "SUCCESS",
  "data": {
    "likes": "스시나 해산물도 좋아하고 달달한 것도 좋아해요",
    "dislikes": "마늘 싫어해요",
    "etc": "가격대가 만 오천원 미만이였으면 좋겠어요"
  },
  "message": null
}
```

### PUT /my/preferences

> 내 취향 수정

#### Request Body

```json
{
  "likes": "스시나 해산물도 좋아하고 달달한 것도 좋아해요",
  "dislikes": "마늘 싫어해요",
  "etc": "가격대가 만 오천원 미만이였으면 좋겠어요"
}
```

#### Response Body

```json
{
  "status": "SUCCESS",
  "data": null,
  "message": null
}
```
