package com.elbloquecadena.storage.block;

import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import com.elbloquecadena.crypto.Crypto;
import com.github.jtmsp.merkletree.IMerkleTree;
import com.github.jtmsp.merkletree.IterateFunction;
import com.github.jtmsp.merkletree.MerkleTree;
import com.github.jtmsp.merkletree.byteable.IByteable;
import com.github.jtmsp.merkletree.crypto.RipeMD160;

/**
 * A Mutable Block<br>
 * Add transactions until block is finished, then make it immutable for persistance
 * 
 * @author wolfposd
 *
 * @param <Transaction>
 *            type of Object this block holds
 */
public class MutableBlock<Transaction extends IByteable> {

    private IMerkleTree<Transaction> tree = new MerkleTree<>();

    private Map<byte[], byte[]> validatorSignatures = new HashMap<>();

    private final long blockHeight;

    public MutableBlock(long blockheight) {
        blockHeight = blockheight;
    }

    public void addTransaction(Transaction t) {
        tree.add(t);
    }

    public boolean addValidatorHash(byte[] validatorPubkey, byte[] validatorHash) {

        boolean success = Crypto.verifySignature(validatorPubkey, tree.getRootHash(), validatorHash);
        if (success) {
            validatorSignatures.put(validatorPubkey, validatorHash);
            return true;
        }
        return false;
    }

    public long getBlockHeight() {
        return blockHeight;
    }

    public byte[] getRootHash() {
        return tree.getRootHash();
    }

    public int getTransactionSize() {
        return tree.size();
    }

    public void iterateNodes(IterateFunction<Transaction> func) {
        tree.iterateNodes(func);
    }

    public String toPrettyTreeString() {
        return tree.toPrettyString();
    }

    public Map<byte[], byte[]> getValidatorSignatures() {
        return Collections.unmodifiableMap(validatorSignatures);
    }

    public ImmutableBlock makeImmutable(byte[] ancestorHash) {

        int size = getTransactionSize();
        byte[] rootHash = getRootHash();

        byte[][] bytes = new byte[size][];

        AtomicInteger i = new AtomicInteger(0);
        iterateNodes(node -> {
            if (node.isLeafNode()) {
                bytes[i.get()] = node.getKey().toByteArray();
                i.incrementAndGet();
            }
            return false;
        });

        ByteBuffer blockHashbuffer = ByteBuffer.allocate(rootHash.length + ancestorHash.length);
        blockHashbuffer.put(rootHash);
        blockHashbuffer.put(ancestorHash);

        byte[] blockhash = RipeMD160.hash(blockHashbuffer.array());

        ValidatorAndHash[] val = new ValidatorAndHash[validatorSignatures.size()];
        int x = 0;
        for (Map.Entry<byte[], byte[]> entry : validatorSignatures.entrySet()) {
            val[x] = new ValidatorAndHash(entry.getKey(), entry.getValue());
            x++;
        }

        return new ImmutableBlock(blockHeight, rootHash, blockhash, ancestorHash, bytes, val);
    }

}
