# 엑셀 업로드/다운로드 headerPath 정리

## 1. headerPath 핵심 개념

| 항목 | 정리 |
|---|---|
| `headerPath` 의미 | 엑셀 다중 헤더 구조를 나타내는 배열 |
| 배열 순서 | 최상위 → 중간 → 최하위 헤더 |
| 예시 | `["col_a", "col_b", "col_c"]` |
| 의미 | 조부모 `col_a` → 부모 `col_b` → 자식 `col_c` |
| 헤더명 규칙 필요 여부 | 없음. 이름이 어떤 값이든 배열 순서만 맞으면 됨 |
| 백엔드 역할 | `headerPath` 배열을 보고 엑셀 헤더 row 생성 |
| 다중 헤더 생성 조건 | 각 leaf 컬럼마다 `headerPath`가 있으면 됨 |

`headerPath`는 헤더명을 파싱하는 방식이 아니라, 배열의 순서를 그대로 엑셀 헤더 행으로 사용하는 구조다.

```js
headerPath: ["조부모", "부모", "자식"]
```

위 구조는 엑셀에서 3단 헤더로 해석된다.

---

## 2. 프론트 구조 정리

| 항목 | 정리 |
|---|---|
| `columnDefs` 생성 방식 | 어떤 방식으로 만들든 상관없음 |
| 중요한 조건 | AG Grid의 `children` 구조만 맞으면 됨 |
| `headerPath` 생성 위치 | `buildExcelColumnMetas()` |
| 호출 위치 | `buildColumns()` |
| 핵심 흐름 | `columnDefs` → `buildColumns()` → `buildExcelColumnMetas()` → `headerPath` 생성 |
| 직접 `headerPath` 만들 필요 | 보통 없음 |
| 그룹 컬럼 | `children`이 있는 컬럼 |
| 실제 데이터 컬럼 | `field`가 있는 leaf 컬럼 |

예를 들어 다음과 같은 `columnDefs`가 있으면:

```js
const columnDefs = [
  {
    headerName: "col_a",
    children: [
      {
        headerName: "col_b",
        children: [
          {
            field: "value",
            headerName: "col_c",
          },
        ],
      },
    ],
  },
];
```

`buildExcelColumnMetas(columnDefs)` 결과는 다음처럼 만들어진다.

```js
[
  {
    field: "value",
    headerName: "col_c",
    parentHeader: "col_b",
    level: 2,
    headerPath: ["col_a", "col_b", "col_c"],
    required: false,
    exampleValue: "",
    dataType: "STRING",
    hidden: false,
    order: 1,
  },
]
```

---

## 3. 다운로드 / 업로드 / 템플릿 공통 방식

| 기능 | 방식 |
|---|---|
| 엑셀 다운로드 | `buildColumns()`로 만든 columns 사용 |
| 템플릿 다운로드 | `buildColumns()`로 만든 columns 사용 |
| 엑셀 업로드 | `buildColumns()`로 만든 columns 사용 |
| 오류행 다운로드 | `buildColumns()`로 만든 columns 사용 |

공통 흐름은 다음과 같다.

```text
현재 화면 columnDefs
→ buildColumns()
→ buildExcelColumnMetas()
→ columns[].headerPath 생성
→ 백엔드 전달
```

다운로드/템플릿 다운로드는 `headerPath`로 엑셀 헤더를 만들고, 업로드는 `headerPath`로 업로드 파일의 헤더를 검증하고 컬럼 index와 field를 매핑한다.

---

## 4. order 정리

| 항목 | 정리 |
|---|---|
| `order` 의미 | 엑셀 컬럼 순서 |
| 데이터 row 순서와 관계 | 없음 |
| 다운로드 시 `order` | A열, B열, C열 같은 컬럼 출력 순서 |
| 업로드 시 `order` | 기대하는 컬럼 순서 |
| 생성 위치 | `buildExcelColumnMetas()`에서 자동 증가 |
| 계산 기준 | `columnDefs`를 왼쪽부터 순회한 순서 |

즉:

```text
columns[].order = 좌우 컬럼 순서
rows[] 순서       = 위아래 데이터 행 순서
```

`order`는 조회된 데이터 행의 순서가 아니라 엑셀 컬럼의 출력 순서다.

---

## 5. 예제행 포함 옵션 정리

| 항목 | 정리 |
|---|---|
| 화면 옵션 | `includeExampleRow` |
| 템플릿 다운로드 | `includeExampleRow`를 백엔드로 직접 넘김 |
| 업로드 | `includeExampleRow`를 직접 넘기지 않음 |
| 업로드에서 사용 방식 | 프론트에서 `dataStartRowIndex` 계산에만 사용 |
| 백엔드가 받는 값 | `option.dataStartRowIndex` |
| 예시 | 3단 헤더 + 예제행 있으면 데이터 시작 index는 `4` |

업로드 흐름은 다음과 같다.

```text
includeExampleRow
→ hasExampleRow
→ dataStartRowIndex 계산
→ 백엔드는 dataStartRowIndex만 보고 데이터 시작 행 판단
```

예를 들어 3단 헤더이고 예제행이 포함되어 있으면:

```text
0행: 1단 헤더
1행: 2단 헤더
2행: 3단 헤더
3행: 예제행
4행: 실제 데이터 시작
```

---

## 6. 백엔드 옵션 기본값

| 항목 | 위치 |
|---|---|
| 단일 헤더 기본값 | `ExcelUploadOption.defaultOption()` |
| 다중 헤더 기본값 | `ExcelUploadOption.defaultMultiHeaderOption(int headerDepth)` |
| ColumnMeta 기준 자동 계산 | `ExcelUploadOption.fromColumns(columns, hasExampleRow)` |
| 실제 업로드 option 결정 | `ExcelUploadService.resolveOption()` |

우선순위는 다음과 같다.

```text
프론트 request.option 있음 → 프론트 option 사용
프론트 request.option 없음 → 백엔드가 columns[].headerPath 기준으로 자동 계산
```

단, 백엔드는 예제행 포함 여부를 자동으로 알 수 없으므로, 예제행 포함 템플릿을 업로드할 때는 프론트에서 `dataStartRowIndex`를 계산해서 option으로 넘기는 방식이 안전하다.

---

## 7. 실무 기준 판단표

| 질문 | 결론 |
|---|---|
| `columnDefs`를 꼭 샘플 방식으로 만들어야 하나? | 아니야 |
| `children` 구조만 있으면 되나? | 맞아 |
| `headerPath`는 자동 생성되나? | 맞아 |
| 헤더명에 규칙이 꼭 필요한가? | 아니야 |
| 배열만 맞게 넘기면 백엔드가 다중 헤더를 만들 수 있나? | 맞아 |
| 다운로드/업로드/템플릿 모두 같은 columns를 쓰나? | 맞아 |
| `order`는 조회 데이터 순서인가? | 아니야, 컬럼 순서야 |
| 예제행 포함 옵션은 업로드 때 그대로 백에 넘기나? | 아니야, 시작행 index 계산에만 사용 |

---

## 8. 한 줄 요약

화면에서 어떤 방식으로든 AG Grid `columnDefs`만 제대로 만들면, 엑셀 처리 시 `buildColumns()`를 호출해서 `headerPath`, `order`, 타입, 필수값 등을 자동 생성하고, 다운로드/업로드/템플릿 모두 그 `columns`를 공통으로 사용한다.
