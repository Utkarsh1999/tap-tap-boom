import wave
import struct
import math
import argparse
import os
import random

SAMPLE_RATE = 44100
MAX_AMP = 31000

def build_wav(filename, samples):
    with wave.open(filename, 'w') as wav_file:
        wav_file.setnchannels(1)
        wav_file.setsampwidth(2)
        wav_file.setframerate(SAMPLE_RATE)
        for s in samples:
            wav_file.writeframesraw(struct.pack('<h', int(max(-32768, min(32767, s)))))

def apply_envelope(samples, attack_ms=2, decay_rate=10, sustain=0.0):
    num_samples = len(samples)
    attack_samples = int((attack_ms / 1000.0) * SAMPLE_RATE)
    out = []
    for i in range(num_samples):
        t = i / SAMPLE_RATE
        env = 1.0
        if i < attack_samples:
            env = i / attack_samples
        
        env *= math.exp(-t * decay_rate)
        
        if i > num_samples - 100:
            env *= (num_samples - i) / 100.0
            
        out.append(samples[i] * env)
    return out

def generate_kick():
    duration = 0.4
    samples = []
    for i in range(int(duration * SAMPLE_RATE)):
        t = i / SAMPLE_RATE
        freq = 150 * math.exp(-t * 30) + 45
        val = math.sin(2 * math.pi * freq * t)
        val += 0.2 * math.sin(4 * math.pi * freq * t) # Harmonic punch
        samples.append(val * MAX_AMP)
    return apply_envelope(samples, attack_ms=2, decay_rate=15)

def generate_snare():
    duration = 0.3
    samples = []
    for i in range(int(duration * SAMPLE_RATE)):
        t = i / SAMPLE_RATE
        noise = random.uniform(-1, 1) * 0.8
        tone = 0.2 * math.sin(2 * math.pi * 180 * t)
        samples.append((noise + tone) * MAX_AMP)
    return apply_envelope(samples, attack_ms=1, decay_rate=20)

def generate_closed_hihat():
    duration = 0.05
    samples = [random.uniform(-1, 1) * MAX_AMP * 0.4 for _ in range(int(duration * SAMPLE_RATE))]
    return apply_envelope(samples, attack_ms=0.5, decay_rate=60)

def generate_open_hihat():
    duration = 0.3
    samples = [random.uniform(-1, 1) * MAX_AMP * 0.4 for _ in range(int(duration * SAMPLE_RATE))]
    return apply_envelope(samples, attack_ms=1, decay_rate=8)

def generate_clap():
    duration = 0.2
    samples = []
    # Claps are often multiple bursts of noise
    for i in range(int(duration * SAMPLE_RATE)):
        t = i / SAMPLE_RATE
        # Three quick micro-bursts before the main decay
        burst = 0
        if t < 0.01: burst = random.uniform(-1,1)
        elif 0.012 < t < 0.022: burst = random.uniform(-1,1)
        elif 0.024 < t < 0.034: burst = random.uniform(-1,1)
        else: burst = random.uniform(-1,1) * math.exp(-(t-0.035)*15)
        samples.append(burst * MAX_AMP * 0.7)
    return apply_envelope(samples, attack_ms=0.5, decay_rate=10)

def generate_tom(freq=120):
    duration = 0.5
    samples = []
    for i in range(int(duration * SAMPLE_RATE)):
        t = i / SAMPLE_RATE
        f = freq * math.exp(-t * 8)
        val = math.sin(2 * math.pi * f * t)
        samples.append(val * MAX_AMP)
    return apply_envelope(samples, attack_ms=5, decay_rate=10)

def generate_rimshot():
    duration = 0.1
    samples = []
    for i in range(int(duration * SAMPLE_RATE)):
        t = i / SAMPLE_RATE
        # High freq resonant burst
        val = math.sin(2 * math.pi * 1200 * t) * math.exp(-t * 50)
        noise = random.uniform(-1, 1) * 0.1
        samples.append((val + noise) * MAX_AMP)
    return apply_envelope(samples, attack_ms=0.5, decay_rate=30)

def generate_shaker():
    duration = 0.15
    samples = []
    for i in range(int(duration * SAMPLE_RATE)):
        t = i / SAMPLE_RATE
        # High pass noise approximation
        noise = random.uniform(-1,1) * (math.sin(2 * math.pi * 5000 * t) * 0.5 + 0.5)
        samples.append(noise * MAX_AMP * 0.3)
    return apply_envelope(samples, attack_ms=10, decay_rate=15) # Longer attack for shaker feel

def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--outdir", default=".", help="Output directory")
    args = parser.parse_args()
    os.makedirs(args.outdir, exist_ok=True)
    
    # Strictly Drum Kit
    build_wav(os.path.join(args.outdir, "kick.wav"), generate_kick())
    build_wav(os.path.join(args.outdir, "snare.wav"), generate_snare())
    build_wav(os.path.join(args.outdir, "hihat_closed.wav"), generate_closed_hihat())
    build_wav(os.path.join(args.outdir, "hihat_open.wav"), generate_open_hihat())
    build_wav(os.path.join(args.outdir, "clap.wav"), generate_clap())
    build_wav(os.path.join(args.outdir, "tom_low.wav"), generate_tom(100))
    build_wav(os.path.join(args.outdir, "tom_high.wav"), generate_tom(180))
    build_wav(os.path.join(args.outdir, "rimshot.wav"), generate_rimshot())
    build_wav(os.path.join(args.outdir, "shaker.wav"), generate_shaker())
    build_wav(os.path.join(args.outdir, "cowbell.wav"), [math.sin(2 * math.pi * 560 * (i/SAMPLE_RATE)) * MAX_AMP * 0.5 for i in range(int(0.2*SAMPLE_RATE))]) # Quick cowbell mock
    
    print(f"Generated Percussion Kit in {args.outdir}")

if __name__ == "__main__":
    main()
