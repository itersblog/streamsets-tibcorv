package com.streamsets.tibcorv.stage.lib;
/*
 * Copyright (c) 1998-$Date: 2016-12-13 12:47:31 -0800 (Tue, 13 Dec 2016) $ TIBCO Software Inc.
 * All Rights Reserved. Confidential & Proprietary.
 * TIB/Rendezvous is protected under US Patent No. 5,187,787.
 * For more information, please contact:
 * TIBCO Software Inc., Palo Alto, California, USA
 *
 */

/*
 * tibrvlisten - generic Rendezvous subscriber
 *
 * This program listens for any number of messages on a specified
 * set of subject(s).  Message(s) received are printed.
 *
 * Some platforms require proper quoting of the arguments to prevent
 * the command line processor from modifying the command arguments.
 *
 * The user may terminate the program by typing Control-C.
 *
 * Optionally the user may specify communication parameters for
 * tibrvTransport_Create.  If none are specified, default values
 * are used.  For information on default values for these parameters,
 * please see the TIBCO/Rendezvous Concepts manual.
 *
 *
 * Examples:
 *
 * Listen to every message published on subject a.b.c:
 *  java tibrvlisten a.b.c
 *
 * Listen to every message published on subjects a.b.c and x.*.Z:
 *  java tibrvlisten a.b.c "x.*.Z"
 *
 * Listen to every system advisory message:
 *  java tibrvlisten "_RV.*.SYSTEM.>"
 *
 * Listen to messages published on subject a.b.c using port 7566:
 *  java tibrvlisten -service 7566 a.b.c
 *
 */

import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tibco.tibrv.*;

public class TibcorvListener implements TibrvMsgCallback {
	private static final Logger log = LoggerFactory.getLogger(TibcorvListener.class);

	private String subjects = null;
	private String service = null;
	private String network = null;
	private String daemon = null;
	private Integer queueBufferSize = null;
	private Integer queuePollWaitMilliseconds = null;

	private Thread thread = null;
	private BlockingQueue<TibrvMsg> queue = null;
	private TibrvTransport transport = null;
	private List<TibrvListener> tibrvListener = null;

	public TibcorvListener(String subjects, String service, String network, String daemon, Integer queueBufferSize,
			Integer queuePollWaitMilliseconds) {
		this.subjects = subjects;
		this.service = service;
		this.network = network;
		this.daemon = daemon;
		this.queueBufferSize = queueBufferSize;
		this.queuePollWaitMilliseconds = queuePollWaitMilliseconds;
	}

	public boolean start() {
		log.info("Starting " + this.getClass().getSimpleName());
		// open Tibrv in native implementation
		try {
			Tibrv.open(Tibrv.IMPL_NATIVE);
		} catch (TibrvException e) {
			log.error("Failed to open Tibrv in native implementation:", e);
			return false;
		}

		// Create RVD transport
		try {
			transport = new TibrvRvdTransport(service, network, daemon);
		} catch (TibrvException e) {
			log.error("Failed to create TibrvRvdTransport:", e);
			return false;
		}

		// Create listeners for specified subjects
		tibrvListener = new ArrayList<TibrvListener>();
		for (String subject : subjects.split(",")) {
			// create listener using default queue
			try {
				tibrvListener.add(new TibrvListener(Tibrv.defaultQueue(), this, transport, subject, null));
				log.info("Listening on: " + subject);
			} catch (TibrvException e) {
				log.error("Failed to create listener: " + subject, e);
				return false;
			}
		}

		queue = new LinkedBlockingQueue<TibrvMsg>(queueBufferSize);
		thread = new Thread("TibcorvDispatchThread") {
			public void run() {
				log.info("Start tibcorv dispatch");
				// dispatch Tibrv events
				while (!this.isInterrupted()) {
					try {
						Tibrv.defaultQueue().dispatch();
					} catch (TibrvException e) {
						log.error("Exception dispatching default queue:", e);
						return;
					} catch (InterruptedException ie) {
						log.info("Thread interrupted");
						return;
					}
				}
				log.info("Tibcorv dispatch stopped");
			};
		};
		thread.start();
		log.info(this.getClass().getSimpleName()+" started");
		return true;
	}

	public void stop() {
		log.info("Stopping " + this.getClass().getSimpleName());
		if (thread != null) {
			thread.interrupt();
			thread = null;
		}
		if (tibrvListener != null) {
			tibrvListener.forEach(e -> e.destroy());
			tibrvListener = null;
		}
		if (transport != null) {
			transport.destroy();
			transport = null;
		}
		if (Tibrv.isValid()) {
			try {
				Tibrv.close();
			} catch (TibrvException e) {
				log.error(e.getMessage(), e);
			}
		}
		if (queue != null) {
			queue.clear();
			queue = null;
		}
		log.info(this.getClass().getSimpleName()+" stopped");
	}

	public TibrvMsg poll() {
		try {
			return queue.poll(queuePollWaitMilliseconds, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
		}
		return null;
	}

	public void onMsg(TibrvListener listener, TibrvMsg msg) {
		queue.offer(msg);
	}

}
