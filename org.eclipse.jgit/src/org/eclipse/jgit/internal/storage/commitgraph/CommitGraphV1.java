/*
 * Copyright (C) 2022, Tencent.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0 which is available at
 * https://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.eclipse.jgit.internal.storage.commitgraph;

import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.ObjectId;

/**
 * Support for the commit-graph v1 format.
 *
 * @see CommitGraph
 */
class CommitGraphV1 implements CommitGraph {

	private final GraphObjectIndex idx;

	private final GraphCommitData commitData;

	CommitGraphV1(GraphObjectIndex index, GraphCommitData commitData) {
		this.idx = index;
		this.commitData = commitData;
	}

	/** {@inheritDoc} */
	@Override
	public int findGraphPosition(AnyObjectId commit) {
		return idx.findGraphPosition(commit);
	}

	/** {@inheritDoc} */
	@Override
	public CommitData getCommitData(int graphPos) {
		if (graphPos < 0 || graphPos >= getCommitCnt()) {
			return null;
		}
		return commitData.getCommitData(graphPos);
	}

	/** {@inheritDoc} */
	@Override
	public ObjectId getObjectId(int graphPos) {
		return idx.getObjectId(graphPos);
	}

	/** {@inheritDoc} */
	@Override
	public long getCommitCnt() {
		return idx.getCommitCnt();
	}
}
