package com.elbloquecadena.storage.block;

import java.util.Objects;

/**
 * An Immutable Block
 * 
 * @author wolfposd
 */
public class ImmutableBlock {

    public final long blockHeight;

    public final byte[] blockHash;

    public final byte[] rootHash;

    public final byte[] ancestorHash;

    public final byte[][] transactions;

    public final ValidatorAndHash[] signatures;

    public ImmutableBlock(long blockheight, byte[] rootHash, byte[] blockhash, byte[] ancestorhash, byte[][] transactions,
            ValidatorAndHash[] validators) {
        this.blockHeight = blockheight;
        this.rootHash = rootHash;
        this.blockHash = blockhash;
        this.ancestorHash = ancestorhash;
        this.transactions = transactions;
        this.signatures = validators;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ImmutableBlock) {
            ImmutableBlock other = (ImmutableBlock) obj;
            return Objects.equals(blockHeight, other.blockHeight) //
                    && Objects.deepEquals(blockHash, other.blockHash) //
                    && Objects.deepEquals(rootHash, other.rootHash) //
                    && Objects.deepEquals(ancestorHash, other.ancestorHash) //
                    && Objects.deepEquals(transactions, other.transactions) //
                    && Objects.deepEquals(signatures, other.signatures);
        } else {
            return false;
        }

    }

    @Override
    public int hashCode() {
        return blockHash.hashCode();
    }
}
