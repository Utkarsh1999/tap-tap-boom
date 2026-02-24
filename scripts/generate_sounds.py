import wave
import struct
import math
import argparse
import os

SAMPLE_RATE = 44100
MAX_AMP = 32767

def build_wav(filename, samples):
    with wave.open(filename, 'w') as wav_file:
        wav_file.setnchannels(1)
        wav_file.setsampwidth(2)
        wav_file.setframerate(SAMPLE_RATE)
        for s in samples:
            wav_file.writeframesraw(struct.pack('<h', int(max(-MAX_AMP, min(MAX_AMP, s)))))

def generate_kick(duration=0.5):
    samples = []
    num_samples = int(duration * SAMPLE_RATE)
    for i in range(num_samples):
        t = i / SAMPLE_RATE
        # Freq envelope: starts high (150Hz), drops quickly to 50Hz
        freq = 150 * math.exp(-t * 20) + 50
        # Amp envelope: sharp attack, exponential decay
        env = math.exp(-t * 10)
        sample = math.sin(2 * math.pi * freq * t) * env * MAX_AMP
        samples.append(sample)
    return samples

def generate_snare(duration=0.4):
    import random
    samples = []
    num_samples = int(duration * SAMPLE_RATE)
    for i in range(num_samples):
        t = i / SAMPLE_RATE
        # Noise component + faint tonal component (180Hz)
        noise = random.uniform(-1, 1)
        tone = math.sin(2 * math.pi * 180 * t)
        # Amp envelope: sharp attack, fast decay
        env = math.exp(-t * 20)
        sample = (noise * 0.8 + tone * 0.2) * env * MAX_AMP
        samples.append(sample)
    return samples

def generate_hihat(duration=0.1):
    import random
    samples = []
    num_samples = int(duration * SAMPLE_RATE)
    for i in range(num_samples):
        t = i / SAMPLE_RATE
        # Pure high-passed noise (simulated by random)
        noise = random.uniform(-1, 1)
        # Very sharp envelope
        env = math.exp(-t * 40)
        sample = noise * env * MAX_AMP * 0.5 # Lower volume
        samples.append(sample)
    return samples

def generate_synth_note(freq, duration=0.8, wave_type='square'):
    samples = []
    num_samples = int(duration * SAMPLE_RATE)
    for i in range(num_samples):
        t = i / SAMPLE_RATE
        # Amp envelope: medium decay
        env = math.exp(-t * 3)
        
        val = 0
        if wave_type == 'square':
            val = 1 if math.sin(2 * math.pi * freq * t) > 0 else -1
        elif wave_type == 'saw':
            val = 2 * (freq * t - math.floor(freq * t + 0.5))
        elif wave_type == 'sine':
            val = math.sin(2 * math.pi * freq * t)
            
        # Add a bit of detuned chorus for fatness
        detune = 1 if math.sin(2 * math.pi * (freq * 1.01) * t) > 0 else -1
        val = (val * 0.7) + (detune * 0.3)
            
        sample = val * env * MAX_AMP * 0.7 # Prevent clipping
        samples.append(sample)
    return samples

def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--outdir", default=".", help="Output directory for wav files")
    args = parser.parse_args()
    
    os.makedirs(args.outdir, exist_ok=True)
    
    # Generate drums
    build_wav(os.path.join(args.outdir, "kick.wav"), generate_kick())
    build_wav(os.path.join(args.outdir, "snare.wav"), generate_snare())
    build_wav(os.path.join(args.outdir, "hihat.wav"), generate_hihat())
    
    # Generate syntax notes (C minor pentatonic scale: C, Eb, F, G, Bb, C, Eb)
    notes = [
        ('a.wav', 261.63, 'square'), # C4
        ('b.wav', 311.13, 'square'), # D#4/Eb4
        ('c.wav', 349.23, 'saw'),    # F4
        ('d.wav', 392.00, 'saw'),    # G4
        ('e.wav', 466.16, 'square'), # A#4/Bb4
        ('f.wav', 523.25, 'sine'),   # C5
        ('g.wav', 622.25, 'sine'),   # D#5/Eb5
    ]
    
    for filename, freq, wave_type in notes:
        build_wav(os.path.join(args.outdir, filename), generate_synth_note(freq, wave_type=wave_type))
        
    print(f"Generated 10 wav files in {args.outdir}")

if __name__ == "__main__":
    main()
