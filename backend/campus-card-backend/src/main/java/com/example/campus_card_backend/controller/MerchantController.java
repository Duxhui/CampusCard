package com.example.campus_card_backend.controller;

import com.example.campus_card_backend.entity.Merchant;
import com.example.campus_card_backend.mapper.MerchantMapper;
import com.example.campus_card_backend.mapper.RecycledMerchantIdMapper;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/merchant")
public class MerchantController {

    private final MerchantMapper merchantMapper;
    private final RecycledMerchantIdMapper recycledMerchantIdMapper;

    public MerchantController(MerchantMapper merchantMapper,
                              RecycledMerchantIdMapper recycledMerchantIdMapper) {
        this.merchantMapper = merchantMapper;
        this.recycledMerchantIdMapper = recycledMerchantIdMapper;
    }

    // 查询所有商户
    @GetMapping("/list")
    public List<Merchant> list() {
        return merchantMapper.findAll();
    }

    // 根据商户编号查询
    @GetMapping("/{merchantId}")
    public Merchant getById(@PathVariable String merchantId) {
        return merchantMapper.findById(merchantId);
    }

    // 按商户类型、营业状态筛选
    @GetMapping("/search")
    public List<Merchant> search(@RequestParam(required = false) String merchantType,
                                 @RequestParam(required = false) Integer businessStatus) {
        return merchantMapper.search(merchantType, businessStatus);
    }

    // 新增商户（自动分配编号）
    @PostMapping
    public Map<String, Object> add(@RequestBody Merchant merchant) {
        Map<String, Object> result = new HashMap<>();

        // 自动分配商户编号：优先回收ID → 否则 M + 自增序号
        if (merchant.getMerchantId() == null || merchant.getMerchantId().trim().isEmpty()) {
            String recycledId = recycledMerchantIdMapper.selectRecycledId();
            if (recycledId != null) {
                merchant.setMerchantId(recycledId);
                recycledMerchantIdMapper.deleteRecycledId(recycledId);
            } else {
                merchant.setMerchantId(generateMerchantId());
            }
        }

        if (merchant.getMerchantName() == null || merchant.getMerchantName().trim().isEmpty()) {
            result.put("success", false);
            result.put("message", "商户名称不能为空");
            return result;
        }

        if (merchant.getMerchantType() == null || merchant.getMerchantType().trim().isEmpty()) {
            result.put("success", false);
            result.put("message", "商户类型不能为空");
            return result;
        }

        if (merchant.getBusinessStatus() == null) {
            merchant.setBusinessStatus(1);
        }

        int rows = merchantMapper.insert(merchant);
        result.put("success", rows > 0);
        result.put("message", rows > 0 ? "新增商户成功" : "新增商户失败");
        result.put("merchantId", merchant.getMerchantId());
        return result;
    }

    // 修改商户
    @PutMapping("/{merchantId}")
    public Map<String, Object> update(@PathVariable String merchantId,
                                      @RequestBody Merchant merchant) {
        Map<String, Object> result = new HashMap<>();

        merchant.setMerchantId(merchantId);

        int rows = merchantMapper.update(merchant);
        result.put("success", rows > 0);
        result.put("message", rows > 0 ? "修改商户成功" : "修改商户失败");
        return result;
    }

    // 启用或停用商户
    @PutMapping("/{merchantId}/status")
    public Map<String, Object> updateStatus(@PathVariable String merchantId,
                                            @RequestParam Integer businessStatus) {
        Map<String, Object> result = new HashMap<>();

        if (businessStatus != 0 && businessStatus != 1) {
            result.put("success", false);
            result.put("message", "营业状态只能为 1营业 或 0停业");
            return result;
        }

        int rows = merchantMapper.updateStatus(merchantId, businessStatus);
        result.put("success", rows > 0);
        result.put("message", rows > 0 ? "商户状态修改成功" : "商户状态修改失败");
        return result;
    }

    // 删除商户：有历史消费记录则不允许删除
    @DeleteMapping("/{merchantId}")
    public Map<String, Object> delete(@PathVariable String merchantId) {
        Map<String, Object> result = new HashMap<>();

        int count = merchantMapper.countConsumptionByMerchantId(merchantId);
        if (count > 0) {
            result.put("success", false);
            result.put("message", "该商户存在历史消费记录，不能删除，可设置为停业");
            return result;
        }

        int rows = merchantMapper.delete(merchantId);
        if (rows > 0) {
            recycledMerchantIdMapper.insertRecycledId(merchantId);
        }
        result.put("success", rows > 0);
        result.put("message", rows > 0 ? "删除商户成功" : "删除商户失败");
        return result;
    }

    private String generateMerchantId() {
        String maxId = merchantMapper.findMaxMerchantId();
        if (maxId == null || maxId.isEmpty()) return "M001";
        int nextNum = Integer.parseInt(maxId.replace("M", "")) + 1;
        return String.format("M%03d", nextNum);
    }
}