---
name: testing
description: >
  单元测试与质量保障。当用户提到测试、单测、验证、回归、覆盖率时激活此技能。
---

# 测试与质量保障 (Testing)

编写单元测试、设计验收方案、保障代码质量。

## 技术栈

| 项目 | 说明 |
|------|------|
| 测试框架 | JUnit 5 |
| Mock 框架 | Mockito |
| 测试目录 | `src/test/java/` |

## Controller 测试模板

```java
@SpringBootTest
@AutoConfigureMockMvc
class XxxControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private XxxService xxxService;

    @Test
    void testGetData_Success() throws Exception {
        when(xxxService.getData(anyLong())).thenReturn(mockData);

        mockMvc.perform(get("/api/xxx")
                .sessionAttr("user", mockUser))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
```

## Service 测试模板

```java
@ExtendWith(MockitoExtension.class)
class XxxServiceTest {
    @InjectMocks
    private XxxService xxxService;

    @Mock
    private XxxMapper xxxMapper;

    @Test
    void testSaveData_Success() {
        when(xxxMapper.insert(any())).thenReturn(1);
        xxxService.saveData(1L, "test");
        verify(xxxMapper, times(1)).insert(any());
    }
}
```

## 覆盖要求

| 层级 | 覆盖要求 |
|------|---------|
| Controller | 100% 接口覆盖 |
| Service | 核心业务逻辑 80%+ |

## 测试场景

每个测试应覆盖：
- ✅ 正常流程
- ✅ 边界条件
- ✅ 异常情况
- ✅ 未登录场景
- ✅ 参数非法
