import wave
import struct
import math
import argparse
import os
import random

SAMPLE_RATE = 44100
MAX_AMP = 31000  # Leave some headroom

def build_wav(filename, samples):
    with wave.open(filename, 'w') as wav_file:
        wav_file.setnchannels(1)
        wav_file.setsampwidth(2)
        wav_file.setframerate(SAMPLE_RATE)
        for s in samples:
            wav_file.writeframesraw(struct.pack('<h', int(max(-32768, min(32767, s)))))

def apply_envelope(samples, attack_ms=10, decay_rate=10):
    num_samples = len(samples)
    attack_samples = int((attack_ms / 1000.0) * SAMPLE_RATE)
    out = []
    for i in range(num_samples):
        t = i / SAMPLE_RATE
        # Linear Attack to prevent pops
        env = 1.0
        if i < attack_samples:
            env = i / attack_samples
        
        # Exponential Decay
        env *= math.exp(-t * decay_rate)
        
        # Final fade-out to zero
        if i > num_samples - 100:
            env *= (num_samples - i) / 100.0
            
        out.append(samples[i] * env)
    return out

def generate_kick():
    duration = 0.4
    num_samples = int(duration * SAMPLE_RATE)
    samples = []
    for i in range(num_samples):
        t = i / SAMPLE_RATE
        # Freq sweep: 150 -> 40Hz
        freq = 150 * math.exp(-t * 25) + 40
        # Fundamental + subtle distortion harmonic
        val = math.sin(2 * math.pi * freq * t)
        val += 0.1 * math.sin(4 * math.pi * freq * t)
        samples.append(val * MAX_AMP)
    return apply_envelope(samples, attack_ms=5, decay_rate=12)

def generate_snare():
    duration = 0.3
    num_samples = int(duration * SAMPLE_RATE)
    samples = []
    for i in range(num_samples):
        t = i / SAMPLE_RATE
        noise = random.uniform(-1, 1)
        # Tone at 180Hz and 330Hz (snare wires)
        tone = 0.5 * math.sin(2 * math.pi * 180 * t) + 0.3 * math.sin(2 * math.pi * 330 * t)
        samples.append((noise * 0.7 + tone * 0.3) * MAX_AMP)
    return apply_envelope(samples, attack_ms=2, decay_rate=18)

def generate_hihat():
    duration = 0.08
    num_samples = int(duration * SAMPLE_RATE)
    samples = []
    for i in range(num_samples):
        noise = random.uniform(-1, 1)
        samples.append(noise * MAX_AMP * 0.5)
    return apply_envelope(samples, attack_ms=1, decay_rate=45)

def generate_synth_note(freq, wave_type='square', richness=0.5):
    duration = 1.0
    num_samples = int(duration * SAMPLE_RATE)
    samples = []
    
    # FM Synthesis parameters
    mod_freq = freq * 2.0
    mod_index = 2.0 * richness
    
    for i in range(num_samples):
        t = i / SAMPLE_RATE
        
        # Modulator
        modulator = math.sin(2 * math.pi * mod_freq * t) * mod_index
        
        # Carrier
        phase = 2 * math.pi * freq * t + modulator
        
        if wave_type == 'square':
            val = 0.6 if math.sin(phase) > 0 else -0.6
            # Formant-like harmonic
            val += 0.2 * math.sin(phase * 3)
        elif wave_type == 'saw':
            # Band-limited-ish saw approximation
            val = 0
            for h in range(1, 5):
                val += (1.0/h) * math.sin(phase * h)
        else: # sine or pluck
            val = math.sin(phase)
            
        samples.append(val * MAX_AMP * 0.6)
    
    return apply_envelope(samples, attack_ms=20, decay_rate=4)

def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--outdir", default=".", help="Output directory")
    args = parser.parse_args()
    os.makedirs(args.outdir, exist_ok=True)
    
    # Dynamic drum kit
    build_wav(os.path.join(args.outdir, "kick.wav"), generate_kick())
    build_wav(os.path.join(args.outdir, "snare.wav"), generate_snare())
    build_wav(os.path.join(args.outdir, "hihat.wav"), generate_hihat())
    
    # Melodic notes with FM character
    notes = [
        ('a.wav', 261.63, 'square'), # C4
        ('b.wav', 311.13, 'square'), # Eb4
        ('c.wav', 349.23, 'saw'),    # F4
        ('d.wav', 392.00, 'saw'),    # G4
        ('e.wav', 466.16, 'square'), # Bb4
        ('f.wav', 523.25, 'sine'),   # C5
        ('g.wav', 622.25, 'sine'),   # Eb5
        
        # New sounds for the rest of the IDs in the json
        ('clap.wav', 800, 'noise'), # (Handled by special logic usually, but we'll use sine-pluck for now)
        ('tom.wav', 120, 'sine'), 
        ('pad.wav', 261.63, 'sine'), 
        ('bass.wav', 65.41, 'saw'),   # C2
        ('bell.wav', 1046.50, 'sine'), # C6
        ('pluck.wav', 440.0, 'sine'),
        ('zap.wav', 880.0, 'saw')
    ]
    
    for filename, freq, wave_type in notes:
        # Special handling for non-melodic percussion if needed, 
        # but the general synth note handles most.
        if filename == 'clap.wav':
            # Quick noise burst for clap
            samples = [random.uniform(-1,1) * MAX_AMP for _ in range(int(0.15 * SAMPLE_RATE))]
            samples = apply_envelope(samples, attack_ms=1, decay_rate=25)
            build_wav(os.path.join(args.outdir, filename), samples)
        elif filename == 'tom.wav':
            # Low freq sine sweep for tom
            samples = [math.sin(2 * math.pi * (120 * math.exp(-i/SAMPLE_RATE * 10)) * (i/SAMPLE_RATE)) * MAX_AMP for i in range(int(0.4 * SAMPLE_RATE))]
            samples = apply_envelope(samples, attack_ms=5, decay_rate=10)
            build_wav(os.path.join(args.outdir, filename), samples)
        else:
            build_wav(os.path.join(args.outdir, filename), generate_synth_note(freq, wave_type=wave_type))
            
    print(f"Synthesized high-quality sounds in {args.outdir}")

if __name__ == "__main__":
    main()
