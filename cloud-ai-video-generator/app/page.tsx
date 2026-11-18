'use client';

import { useState } from 'react';

export default function Home() {
  const [prompt, setPrompt] = useState('');
  const [duration, setDuration] = useState('5');
  const [ratio, setRatio] = useState('1280:720');
  const [status, setStatus] = useState('');
  const [error, setError] = useState('');
  const [videoUrl, setVideoUrl] = useState('');

  async function generateVideo() {
    setError('');
    setVideoUrl('');
    setStatus('Generating video...');

    try {
      const response = await fetch('/api/generate', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ prompt, duration: Number(duration), ratio })
      });

      const data = await response.json();

      if (!response.ok) {
        throw new Error(data.error || 'Video generation failed.');
      }

      setVideoUrl(data.videoUrl);
      setStatus('Video generated successfully.');
    } catch (err) {
      setStatus('');
      setError(err instanceof Error ? err.message : 'Something went wrong.');
    }
  }

  return (
    <main className="container">
      <section className="card">
        <h1>Cloud AI Video Generator</h1>
        <p className="subtitle">Turn a text prompt into an AI-generated video.</p>

        <label htmlFor="prompt">Prompt</label>
        <textarea
          id="prompt"
          value={prompt}
          onChange={(e) => setPrompt(e.target.value)}
          placeholder="A cinematic drone shot over a futuristic city at sunset..."
          rows={7}
        />

        <div className="grid">
          <div>
            <label htmlFor="duration">Duration</label>
            <select id="duration" value={duration} onChange={(e) => setDuration(e.target.value)}>
              {Array.from({ length: 9 }, (_, i) => i + 2).map((seconds) => (
                <option key={seconds} value={seconds}>{seconds} seconds</option>
              ))}
            </select>
          </div>

          <div>
            <label htmlFor="ratio">Aspect ratio</label>
            <select id="ratio" value={ratio} onChange={(e) => setRatio(e.target.value)}>
              <option value="1280:720">Landscape 16:9</option>
              <option value="720:1280">Portrait 9:16</option>
            </select>
          </div>
        </div>

        <button onClick={generateVideo} disabled={!prompt.trim() || status === 'Generating video...'}>
          {status === 'Generating video...' ? 'Generating...' : 'Generate Video'}
        </button>

        {status && <p className="status">{status}</p>}
        {error && <p className="error">{error}</p>}

        {videoUrl && (
          <div className="result">
            <video controls src={videoUrl} />
            <a href={videoUrl} target="_blank" rel="noreferrer">Open generated video</a>
          </div>
        )}
      </section>
    </main>
  );
}
