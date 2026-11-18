import { NextResponse } from 'next/server';
import RunwayML, { TaskFailedError } from '@runwayml/sdk';

export const maxDuration = 300;

export async function POST(request: Request) {
  try {
    const { prompt, duration, ratio } = await request.json();

    if (!prompt || typeof prompt !== 'string') {
      return NextResponse.json({ error: 'Prompt is required.' }, { status: 400 });
    }

    if (!Number.isInteger(duration) || duration < 2 || duration > 10) {
      return NextResponse.json({ error: 'Duration must be between 2 and 10 seconds.' }, { status: 400 });
    }

    if (ratio !== '1280:720' && ratio !== '720:1280') {
      return NextResponse.json({ error: 'Invalid aspect ratio.' }, { status: 400 });
    }

    if (!process.env.RUNWAYML_API_SECRET) {
      return NextResponse.json({ error: 'RUNWAYML_API_SECRET is not configured.' }, { status: 500 });
    }

    const client = new RunwayML({
      apiSecret: process.env.RUNWAYML_API_SECRET
    });

    const task = await client.textToVideo.create({
      model: 'seedance2_5',
      promptText: prompt,
      ratio,
      duration
    }).waitForTaskOutput();

    const videoUrl = task.output?.[0];

    if (!videoUrl) {
      return NextResponse.json({ error: 'The API returned no video URL.' }, { status: 502 });
    }

    return NextResponse.json({ videoUrl });
  } catch (error) {
    if (error instanceof TaskFailedError) {
      return NextResponse.json({ error: error.message || 'Runway task failed.' }, { status: 502 });
    }

    console.error(error);
    return NextResponse.json({ error: 'Video generation failed.' }, { status: 500 });
  }
}
