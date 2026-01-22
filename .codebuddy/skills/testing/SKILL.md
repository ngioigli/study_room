# 🧪 Skill: 测试与质量保障 (Testing)

> **技能 ID**: `testing`
> **触发关键词**: 测试、单测、验证、回归、覆盖率

## 1. 技能职责

- 编写单元测试
- 设计验收方案
- 执行回归测试
- 保障代码质量

## 2. 技术约束

| 项目 | 说明 |
|------|------|
| **测试框架** | JUnit 5 |
| **Mock 框架** | Mockito |
| **测试目录** | `src/test/java/` |

## 3. 测试规范

### Controller 测试

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
        // Given
        when(xxxService.getData(anyLong())).thenReturn(mockData);

        // When & Then
        mockMvc.perform(get("/api/xxx")
                .sessionAttr("user", mockUser))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
```

### Service 测试

```java
@ExtendWith(MockitoExtension.class)
class XxxServiceTest {

    @InjectMocks
    private XxxService xxxService;

    @Mock
    private XxxMapper xxxMapper;

    @Test
    void testSaveData_Success() {
        // Given
        when(xxxMapper.insert(any())).thenReturn(1);

        // When
        xxxService.saveData(1L, "test");

        // Then
        verify(xxxMapper, times(1)).insert(any());
    }
}
```

## 4. 测试覆盖要求

| 层级 | 覆盖要求 |
|------|---------|
| Controller | 100% 接口覆盖 |
| Service | 核心业务逻辑 80%+ |

## 5. 测试场景

每个测试应覆盖：

- ✅ 正常流程
- ✅ 边界条件
- ✅ 异常情况
- ✅ 未登录场景
- ✅ 参数非法

## 6. 验收方案模板

```markdown
### 验收清单

#### 功能验证
- [ ] 正常流程可用
- [ ] 数据正确保存

#### 异常验证
- [ ] 未登录提示正确
- [ ] 参数错误提示正确

#### 回归验证
- [ ] 相关功能不受影响
```

## 7. 测试检查清单

- [ ] Controller 测试覆盖所有接口
- [ ] Service 核心逻辑有测试
- [ ] Mock 正确使用
- [ ] 测试可重复执行
- [ ] 测试独立无依赖
