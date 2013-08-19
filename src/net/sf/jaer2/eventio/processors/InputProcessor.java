package net.sf.jaer2.eventio.processors;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import net.sf.jaer2.chips.Chip;
import net.sf.jaer2.eventio.ProcessorChain;
import net.sf.jaer2.eventio.eventpackets.EventPacketContainer;
import net.sf.jaer2.eventio.eventpackets.raw.RawEventPacket;
import net.sf.jaer2.eventio.events.Event;
import net.sf.jaer2.eventio.sources.Source;

public final class InputProcessor extends Processor {
	private final BlockingQueue<RawEventPacket> inputQueue = new ArrayBlockingQueue<>(32);
	private final List<RawEventPacket> inputToProcess = new ArrayList<>(32);

	private Source connectedSource;
	private Chip interpreterChip;

	public InputProcessor(final ProcessorChain chain) {
		super(chain);
	}

	public Source getConnectedSource() {
		return connectedSource;
	}

	public void setConnectedSource(final Source source) {
		connectedSource = source;
	}

	public Chip getInterpreterChip() {
		return interpreterChip;
	}

	public void setInterpreterChip(final Chip chip) {
		interpreterChip = chip;

		// Regenerate output types based on what the Chip can produce.
		if (interpreterChip != null) {
			regenerateAdditionalOutputTypes(interpreterChip.getEventTypes());
		}
	}

	@Override
	public void run() {
		while (!Thread.currentThread().isInterrupted()) {
			// First forward currently waiting packets from previous processors.
			if (workQueue.drainTo(workToProcess) != 0) {
				getNextProcessor().addAll(workToProcess);

				workToProcess.clear();
			}

			// Then see if there is any conversion to be done.
			if (inputQueue.drainTo(inputToProcess) == 0) {
				// Nothing to be done, next cycle!
				continue;
			}

			// Convert raw events into real ones.
			for (RawEventPacket inRawEventPacket : inputToProcess) {
				EventPacketContainer pktContainer = interpreterChip.extractEventPacketContainer(inRawEventPacket);

				if (pktContainer != null) {
					getNextProcessor().add(pktContainer);
				}
			}

			inputToProcess.clear();
		}
	}

	@Override
	protected void setCompatibleInputTypes(@SuppressWarnings("unused") final Set<Class<? extends Event>> inputs) {
		// Empty, doesn't process any inputs at all.
	}

	@Override
	protected void setAdditionalOutputTypes(@SuppressWarnings("unused") final Set<Class<? extends Event>> outputs) {
		// No events exist here by default, they depend on what the selected
		// Chip can output.
	}

	public void addToInput(final RawEventPacket rawEventPacket) {
		inputQueue.offer(rawEventPacket);
	}

	public void addAllToInput(final Collection<RawEventPacket> rawEventPackets) {
		for (final RawEventPacket rawEventPacket : rawEventPackets) {
			inputQueue.offer(rawEventPacket);
		}
	}
}
