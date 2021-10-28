package com.overminddl1.over_ecs;

public sealed interface StorageType permits StorageType.SparseSetImpl, StorageType.TableImpl {
	StorageType Table = new StorageType.TableImpl();
	StorageType SparseSet = new StorageType.SparseSetImpl();

	final class TableImpl implements StorageType {
		private TableImpl() {
		}
	}

	final class SparseSetImpl implements StorageType {
		private SparseSetImpl() {
		}
	}
}
