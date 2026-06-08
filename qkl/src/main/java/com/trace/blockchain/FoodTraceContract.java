package com.trace.blockchain;

import org.fisco.bcos.sdk.abi.TypeReference;
import org.fisco.bcos.sdk.abi.datatypes.*;
import org.fisco.bcos.sdk.abi.datatypes.generated.Uint256;
import org.fisco.bcos.sdk.client.Client;
import org.fisco.bcos.sdk.contract.Contract;
import org.fisco.bcos.sdk.crypto.keypair.CryptoKeyPair;
import org.fisco.bcos.sdk.model.TransactionReceipt;
import org.fisco.bcos.sdk.transaction.model.exception.ContractException;

import java.io.InputStream;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * FoodTrace 智能合约 Java Wrapper
 * 对应 FoodTrace.sol 的链上方法调用
 *
 * ABI 和 BIN 从 classpath:fiscobcos/ 目录下的文件中读取
 * 不需要在代码里硬编码，方便后续替换
 */
@SuppressWarnings("rawtypes")
public class FoodTraceContract extends Contract {

    // 从文件加载 ABI 和 BIN
    private static final String ABI;
    private static final String BINARY;

    static {
        ABI = loadResource("fiscobcos/FoodTrace.abi");
        BINARY = loadResource("fiscobcos/FoodTrace.bin");
    }

    /**
     * 从 classpath 读取资源文件内容
     */
    private static String loadResource(String path) {
        try (InputStream is = FoodTraceContract.class.getClassLoader().getResourceAsStream(path)) {
            if (is != null) {
                return new String(is.readAllBytes(), StandardCharsets.UTF_8).trim();
            }
        } catch (Exception e) {
            // 读取失败不影响模拟模式运行
        }
        return "";
    }

    // 合约方法名常量
    public static final String FUNC_SUBMITBATCH = "submitBatch";
    public static final String FUNC_SUBMITTEST = "submitTest";
    public static final String FUNC_SUBMITLOGISTICS = "submitLogistics";
    public static final String FUNC_SUBMITSTORAGE = "submitStorage";
    public static final String FUNC_GETRECORDCOUNT = "getRecordCount";
    public static final String FUNC_GETRECORD = "getRecord";
    public static final String FUNC_VERIFYHASH = "verifyHash";

    protected FoodTraceContract(String contractAddress, Client client, CryptoKeyPair credential) {
        super(BINARY, contractAddress, client, credential);
    }

    /**
     * 加载已部署的合约实例
     */
    public static FoodTraceContract load(String contractAddress, Client client, CryptoKeyPair credential) {
        return new FoodTraceContract(contractAddress, client, credential);
    }

    public static String getABI() {
        return ABI;
    }

    public static String getBINARY() {
        return BINARY;
    }

    // ========== 写方法（发送交易） ==========

    /**
     * 提交生产批次上链
     */
    public TransactionReceipt submitBatch(BigInteger businessId, String batchNo,
                                          String dataHash, String dataJson) throws ContractException {
        final Function function = new Function(
                FUNC_SUBMITBATCH,
                Arrays.asList(
                        new Uint256(businessId),
                        new Utf8String(batchNo),
                        new Utf8String(dataHash),
                        new Utf8String(dataJson)),
                Collections.emptyList());
        return executeTransaction(function);
    }

    /**
     * 提交质检记录上链
     */
    public TransactionReceipt submitTest(BigInteger businessId, String batchNo,
                                         String dataHash, String dataJson) throws ContractException {
        final Function function = new Function(
                FUNC_SUBMITTEST,
                Arrays.asList(
                        new Uint256(businessId),
                        new Utf8String(batchNo),
                        new Utf8String(dataHash),
                        new Utf8String(dataJson)),
                Collections.emptyList());
        return executeTransaction(function);
    }

    /**
     * 提交物流运输上链
     */
    public TransactionReceipt submitLogistics(BigInteger businessId, String batchNo,
                                              String dataHash, String dataJson) throws ContractException {
        final Function function = new Function(
                FUNC_SUBMITLOGISTICS,
                Arrays.asList(
                        new Uint256(businessId),
                        new Utf8String(batchNo),
                        new Utf8String(dataHash),
                        new Utf8String(dataJson)),
                Collections.emptyList());
        return executeTransaction(function);
    }

    /**
     * 提交入库记录上链
     */
    public TransactionReceipt submitStorage(BigInteger businessId, String batchNo,
                                            String dataHash, String dataJson) throws ContractException {
        final Function function = new Function(
                FUNC_SUBMITSTORAGE,
                Arrays.asList(
                        new Uint256(businessId),
                        new Utf8String(batchNo),
                        new Utf8String(dataHash),
                        new Utf8String(dataJson)),
                Collections.emptyList());
        return executeTransaction(function);
    }

    // ========== 读方法（call，不上链） ==========

    /**
     * 查询某批次上链记录数量
     */
    public BigInteger getRecordCount(String batchNo) throws ContractException {
        final Function function = new Function(
                FUNC_GETRECORDCOUNT,
                Arrays.asList(new Utf8String(batchNo)),
                Arrays.asList(new TypeReference<Uint256>() {}));
        return executeCallWithSingleValueReturn(function, BigInteger.class);
    }

    /**
     * 查询某批次第index条上链记录
     */
    public ChainRecord getRecord(String batchNo, BigInteger index) throws ContractException {
        final Function function = new Function(
                FUNC_GETRECORD,
                Arrays.asList(new Utf8String(batchNo), new Uint256(index)),
                Arrays.asList(
                        new TypeReference<Utf8String>() {},
                        new TypeReference<Uint256>() {},
                        new TypeReference<Utf8String>() {},
                        new TypeReference<Uint256>() {},
                        new TypeReference<Utf8String>() {}));
        List<Type> results = executeCallWithMultipleValueReturn(function);
        return new ChainRecord(
                (String) results.get(0).getValue(),
                (BigInteger) results.get(1).getValue(),
                (String) results.get(2).getValue(),
                (BigInteger) results.get(3).getValue(),
                (String) results.get(4).getValue());
    }

    /**
     * 验证数据完整性（链上摘要比对）
     */
    public Boolean verifyHash(String batchNo, BigInteger index, String hash) throws ContractException {
        final Function function = new Function(
                FUNC_VERIFYHASH,
                Arrays.asList(
                        new Utf8String(batchNo),
                        new Uint256(index),
                        new Utf8String(hash)),
                Arrays.asList(new TypeReference<Bool>() {}));
        return executeCallWithSingleValueReturn(function, Boolean.class);
    }

    /**
     * 链上记录数据结构
     */
    public static class ChainRecord {
        public final String businessType;
        public final BigInteger businessId;
        public final String dataHash;
        public final BigInteger timestamp;
        public final String dataJson;

        public ChainRecord(String businessType, BigInteger businessId,
                           String dataHash, BigInteger timestamp, String dataJson) {
            this.businessType = businessType;
            this.businessId = businessId;
            this.dataHash = dataHash;
            this.timestamp = timestamp;
            this.dataJson = dataJson;
        }
    }
}
